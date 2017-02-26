$(document).ready(function() {
	$('.nav-trigger').click(function() {
		$('.side-nav').toggleClass('visible');
	});
});

setTimeout(fade_out, 3000);

function fade_out() {
    $("#fadeOut").fadeOut().empty();
}