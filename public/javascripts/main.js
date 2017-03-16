$(document).ready(function() {
	$('.nav-trigger').click(function() {
		$('.side-nav').toggleClass('visible');
	});
});

setTimeout(fade_out, 3000);

function fade_out() {
    $("#fadeOut").fadeOut().empty();
}

function unhide(clickedButton, divID) {
    var item = document.getElementById(divID);
    if (item) {
        if(item.className=='hidden'){
            item.className = 'unhidden' ;
            clickedButton.value = 'Cancel'
        }else{
            item.className = 'hidden';
            clickedButton.value = 'Reschedule Appointment'
        }
    }}