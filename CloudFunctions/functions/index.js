const functions = require('firebase-functions');
const admin = require('firebase-admin');
// // Create and Deploy Your First Cloud Functions
// // https://firebase.google.com/docs/functions/write-firebase-functions
//
// exports.helloWorld = functions.https.onRequest((request, response) => {
//  response.send("Hello from Firebase!");
// });

admin.initializeApp();

var db = admin.firestore();

exports.onCreateUser = 
	functions.auth.user().onCreate((userRecord, context) => {

		const email = userRecord.email;
		const uid = userRecord.uid;
		const displayName = userRecord.displayName;

		console.log("User: " + uid + "|" + userRecord.displayName);
        if(displayName){
            db.collection("users").doc(uid).set({
                'uid': uid,
                'email' : email,
                'displayName' : displayName
            });
        } else {
            db.collection("users").doc(uid).set({
                'uid': uid,
                'email' : email,
            });
        }


		const resolveInvite = db.collection("invites").where("email", "==", email).get()
		.then(snapshot =>{
			snapshot.forEach(inv =>{
				let invite = inv.data();
				const workgroupId = invite.workgroupId;
				console.log("Invite: " + invite + "|" + invite.email + "|" + invite.workgroupId);
				return db.collection('workgroups').doc(workgroupId).get()
		 		.then(wk => {
		 			if(wk.exists){
		 				let workgroup = wk.data();
		 				console.log('Workgroup data: \n' + wk + '|' + workgroup + '|' + workgroup.workgroupId + '|' + workgroup.displayName);
		 				const usrRef = db.collection("users").doc(uid);
		 				return Promise.all([userToGlobal(uid, workgroupId), workgroupToUser(usrRef, workgroup), inv.ref.delete()]);
		 			}
		 		});
			});
		})
		.catch(function(error){
    		console.log("Error fetching user data:", error);
  		});
		return resolveInvite;

	});

/*exports.onDeleteUser = 
	functions.auth.user().onDelete(event => {
		const user = event.data;
		const uid = user.uid;

		var userRef = db.collection("users").doc(uid).delete();

	});
*/

exports.onInvite =
	 functions.firestore.document('invites/{inviteId}').onCreate((snap, context) => {
	 	const newInv = snap.data();

	 	const email = newInv.email;
	 	const workgroupId = newInv.workgroupId;
	 	
	 	console.log(email + ' - ' + workgroupId)

	 	return db.collection('workgroups').doc(workgroupId).get()
	 		.then(wk => {
	 			if(wk.exists){
	 				let workgroup = wk.data()
	 				console.log('Workgroup data: \n' + wk + '|' + workgroup + '|' + workgroup.workgroupId + '|' + workgroup.displayName);

	 				return db.collection('users').where('email', '==', email).get()
				 		.then(snapshot => {
				 			snapshot.forEach(usr =>{
				 				if(usr.exists){
				 					//User exists in the system
				 					let user = usr.data();
				 					console.log('User data: \n' + usr + '|' + user + '|' + user.uid + '|' + user.displayName);
				 					//Copy user uid to global workgroup
				 					//Copy workgroup to user workgroup list
				 					//Delete invite
				 					return Promise.all([userToGlobal(user.uid, workgroupId), workgroupToUser(usr.ref, workgroup), snap.ref.delete()]);
				 				} else{
				 					//User doesn't exist in the system
				 					//Do nothing, the process will be done on create
				 					console.log("User doesn't exist, email: " + email + "|" + usr);
				 				}
				 			});
				 		});
				 	
	 			} else {
	 				console.log('No workgroup with id ' + workgroupId);
	 			}
	 		})
	 		.catch(err => {
	 			console.log('Error getting workgroup', err);
	 		});

	 });

	 function userToGlobal(userUid, wkId){

	 	return admin.auth().getUser(userUid)
          		.then(function(userRecordName){
          			const displayName = userRecordName.displayName;
        			console.log("DisplayName: " + displayName);
        			db.collection("users").doc(userUid).set({
        				'displayName' : displayName
        			});

        			var shortName = slugify(displayName);
        			var split = shortName.trim().split(/\s+/);

                    if(split.length == 1){
                        shortName = shortName.substring(0,3);
                    } else if(split.length == 2){
        			    shortName = split[0].substring(0,2);
        			    shortName +=split[1].substring(0,1);
        			} else {
        			    shortName = split[0].substring(0,2);
                        shortName +=split[1].substring(0,1);
                        shortName +=split[2].substring(0,1);
        			}

        			return db.collection('workgroups').doc(wkId).collection('users').doc(userUid).set({uid: userUid, shortName: shortName, active: true});

        		})
        		.catch(function(error){
            		console.log("Error get email:", error);
          		});
	 }

    function slugify (str) {
        var map = {
            'a' : 'á|à|ã|â|À|Á|Ã|Â',
            'e' : 'é|è|ê|É|È|Ê',
            'i' : 'í|ì|î|Í|Ì|Î',
            'o' : 'ó|ò|ô|õ|Ó|Ò|Ô|Õ',
            'u' : 'ú|ù|û|ü|Ú|Ù|Û|Ü',
            'c' : 'ç|Ç',
            'n' : 'ñ|Ñ'
        };

        for (var pattern in map) {
            str = str.replace(new RegExp(map[pattern], 'g'), pattern);
        };

        return str;
    };

	 function workgroupToUser(userRef, workgroup){
	 	return userRef.collection('workgroups').doc(workgroup.workgroupId).set({
	 		workgroupId : workgroup.workgroupId,
	 		displayName : workgroup.displayName,
	 		info : workgroup.info,
	 		role : 'USER'
	 	});
	 };

exports.onTokenChange =
	 functions.firestore.document('messaging/{userId}/devices/{device}').onWrite((snap, context) => {

	    const uid = context.params.userId;
	    const topic = "msg_" + uid;

	    if(snap.after.exists){
	        const device = snap.after.data();
            console.log("Subscribe " + device.deviceId + "|" + device.token);
            return admin.messaging().subscribeToTopic(device.token, topic)
                .then(function(response) {
                    console.log("[" + device.deviceId + "|" + device.token + "] subscribed to " + topic + " response: " + response);
                })
                .catch(function(error) {
                    console.log("[" + device.deviceId + "|" + device.token + "] error subscribing to " + topic + "with error: " + error);
                });
	    } else {
	        const device = snap.before.data();
	        console.log("Unsubscribe " + device.deviceId + "|" + device.token);
	        return admin.messaging().unsubscribeFromTopic(device.token, topic)
                .then(function(response) {
                    console.log("[" + device.deviceId + "|" + device.token + "] unsubscribed from " + topic + " response: " + response);
                })
                .catch(function(error) {
                    console.log("[" + device.deviceId + "|" + device.token + "] error unsubscribing from " + topic + "with error: " + error);
                });
	    }

	 });

exports.onScheduleUpdated =
	 functions.firestore.document('messaging/{userId}/workgroups/{workgroupId}').onCreate((snap, context) => {
        const uid = context.params.userId;
        const wkId = context.params.workgroupId;
        const wkName = snap.get('displayName');
        console.log(uid + "|" + wkId + "|" + wkName);
        const topic = "msg_" + uid;


        var message = {
            android: {
                ttl: 3600*24*14,
                priority: 'high',
                data: {
                    type: "schedule",
                    workgroupId: wkId,
                    displayName: wkName
                }
            },
            topic: topic
        };

        return admin.messaging().send(message)
            .then(function(response){
                console.log("Message sent to " + uid + " updated " + wkId);
                snap.ref.delete();
            })
            .catch(function(error) {
                console.log("Error sending message to " + uid + " updated " + wkId + ": " + error);
                snap.ref.delete();
            });
	 });


exports.onChangeRequest =
    functions.firestore.document('workgroups/{workgroupId}/changerequests/{requestId}').onWrite((snap, context) => {
        const REQUESTED = "requested";
        const ACCEPTED_USER = "acceptedUser";
        const ACCEPTED_MANAGER = "acceptedManager";
        const APPROVED = "approved";
        const CANCELLED = "cancelled";
        const CONFLICT = "conflict";
        const DENIED_USER = "deniedUser";
        const DENIED_MANAGER = "deniedManager"


        const wkId = context.params.workgroupId;
        const reqId = context.params.requestId;

        return admin.firestore().collection('workgroups').doc(wkId).get()
        .then(wk => {
            if(wk.exists){
                const wkName = wk.data().displayName;
                if(snap.after.exists){
                    const request = snap.after.data();
                    const state = request.state;
                    const reqId = request.id;
                    var destUid;
                    console.log("State of " + reqId + ": " + state);

                    //Create or update
                    switch(state) {
                        case "requested":
                            console.log("State of " + reqId + ": " + state);
                            destUid = request.otherShift.userId;

                            sendChangeNotif(wkName, destUid, reqId, REQUESTED);
                            break;
                        case "accepted":
                            destUid = request.ownShift.userId;
                            sendChangeNotif(wkName, destUid, reqId, ACCEPTED_USER);
                            //TODO: send to manager
                            // GET MANAGER UID
                            // sendChangeNotif(wkName, destUid, ACCEPTED_MANAGER);
                            break;
                        case "approved":
                            destUid = request.ownShift.userId;
                            sendChangeNotif(wkName, destUid, reqId, APPROVED);
                            destUid = request.otherShift.userId;
                            sendChangeNotif(wkName, destUid, reqId, APPROVED);
                            break;
                        case "cancelled":
                            destUid = request.ownShift.userId;
                            sendChangeNotif(wkName, destUid, reqId, CANCELLED);
                            destUid = request.otherShift.userId;
                            sendChangeNotif(wkName, destUid, reqId, CANCELLED);
                            break;
                        case "conflict":
                            destUid = request.ownShift.userId;
                            sendChangeNotif(wkName, destUid, reqId, CONFLICT);
                            break;
                        case "deniedUser":
                            destUid = request.ownShift.userId;
                            sendChangeNotif(wkName, destUid, reqId, DENIED_USER);
                            break;
                        case "deniedManager":
                            destUid = request.ownShift.userId;
                            sendChangeNotif(wkName, destUid, reqId, DENIED_MANAGER);
                            destUid = request.otherShift.userId;
                            sendChangeNotif(wkName, destUid, reqId, DENIED_MANAGER);
                            break;
                    }
                 }
            }
        });
    });

    function sendChangeNotif(wkName, destUid, reqId, change){
        console.log("Dest user: " + destUid);
        const topic = "msg_" + destUid;

        var message = {
            android: {
                ttl: 3600*24*14,
                priority: 'high',
                data: {
                    type: "change",
                    change: change,
                    id: reqId,
                    displayName: wkName
                }
            },
            topic: topic
        };
        return admin.messaging().send(message)
            .then(function(response){
                console.log("Message sent to " + destUid + " change: " + change + " in " + wkName);
            })
            .catch(function(error) {
                console.log("Error sending message to " + destUid + " change: " + change + " in " + wkName + ": " + error);
            });
    }
	