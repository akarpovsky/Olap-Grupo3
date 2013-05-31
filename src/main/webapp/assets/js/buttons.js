$(document).ready(function() {
	// Change Image button
	$("#editphoto").live("click", function() {
		document.location.href = "../user/editPhoto";
	});

	// Edit Info Button
	$("#editinfo").live("click", function() {
		document.location.href = "../user/editinfo";
	});

	// Edit Pass Button
	$("#editpass").live("click", function() {
		document.location.href = "../user/editpass";
	});

	// Upcoming Event Button
	$("#upcomingevents").live("click", function() {
		document.location.href = "../event/upcomingEvents";
	});

	// Friend Suggestions Button
	$("#suggestions").live("click", function(){
		document.location.href = "../user/friendSuggest";
	}); 
	
	// Notifications Button
	$("#notifications").live("click", function() {
		document.location.href = "../profile/notifications";
	});
	
	// Uncoming Birthdays Button
	$("#upcomingbirthdays").live("click", function() {
		document.location.href = "../user/upcomingBirthdays";
	});
	
	// Edit Birthdays Visibility
	$("#editupcomingbirthdaynumber").live("click", function() {
		document.location.href = "../profile/editUpcomingBirthdayNumber";
	});
	
});