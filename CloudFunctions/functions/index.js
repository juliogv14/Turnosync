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

		console.log("User: " + uid + "|" + userRecord.displayName);


		//TODO test if necessary
		const usrToDb = admin.auth().getUser(uid)
  		.then(function(userRecordName){
  			const displayName = userRecordName.displayName;
			console.log("DisplayName: " + displayName);
			db.collection("users").doc(uid).set({
				'uid': uid,
				'email' : email,
				'displayName' : displayName
			});
		
		})
		.catch(function(error){
    		console.log("Error get email:", error);
  		});

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
		return Promise.all([usrToDb, resolveInvite]);

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
	 	return db.collection('workgroups').doc(wkId).collection('users').doc(userUid).set({uid: userUid});
	 }

	 function workgroupToUser(userRef, workgroup){
	 	return userRef.collection('workgroups').doc(workgroup.workgroupId).set({
	 		workgroupId : workgroup.workgroupId,
	 		displayName : workgroup.displayName,
	 		info : workgroup.info,
	 		role : 'USER'
	 	});
	 }
	