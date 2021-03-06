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
    }
}

function unhidePassword(clickedButton, divID){
    var item = document.getElementById(divID);
    if (item) {
        if(item.className=='hidden'){
            item.className = 'unhidden' ;
            clickedButton.value = 'Cancel'
        }else{
            item.className = 'hidden';
            clickedButton.value = 'Change Password'
        }
    }
}

function sortTable(n) {
    var table, rows, switching, i, x, y, shouldSwitch, dir, switchcount = 0;
    table = document.getElementById("sortableTable");
    switching = true;
    //Set the sorting direction to ascending:
    dir = "asc";
    /*Make a loop that will continue until
     no switching has been done:*/
    while (switching) {
        //start by saying: no switching is done:
        switching = false;
        rows = table.getElementsByTagName("TR");
        /*Loop through all table rows (except the
         first, which contains table headers):*/
        for (i = 1; i < (rows.length - 1); i++) {
            //start by saying there should be no switching:
            shouldSwitch = false;
            /*Get the two elements you want to compare,
             one from current row and one from the next:*/
            x = rows[i].getElementsByTagName("TD")[n];
            y = rows[i + 1].getElementsByTagName("TD")[n];
            /*check if the two rows should switch place,
             based on the direction, asc or desc:*/
            if (dir == "asc") {
                if (x.innerHTML.toLowerCase() > y.innerHTML.toLowerCase()) {
                    //if so, mark as a switch and break the loop:
                    shouldSwitch= true;
                    break;
                }
            } else if (dir == "desc") {
                if (x.innerHTML.toLowerCase() < y.innerHTML.toLowerCase()) {
                    //if so, mark as a switch and break the loop:
                    shouldSwitch= true;
                    break;
                }
            }
        }
        if (shouldSwitch) {
            /*If a switch has been marked, make the switch
             and mark that a switch has been done:*/
            rows[i].parentNode.insertBefore(rows[i + 1], rows[i]);
            switching = true;
            //Each time a switch is done, increase this count by 1:
            switchcount ++;
        } else {
            /*If no switching has been done AND the direction is "asc",
             set the direction to "desc" and run the while loop again.*/
            if (switchcount == 0 && dir == "asc") {
                dir = "desc";
                switching = true;
            }
        }
    }
}

    $(document).ready(function() {
        $('#password').keyup(function() {
            $('#result').html(checkStrength($('#password').val()))
        });
        function checkStrength(password) {
            var strength = 0;
            if (password.length < 6) {
                $('#result').removeClass();
                $('#result').text("Too Short");
                return 'Too short'
            }
            if (password.length > 7) strength += 1
// If password contains both lower and uppercase characters, increase strength value.
            if (password.match(/([a-z].*[A-Z])|([A-Z].*[a-z])/)) strength += 1
// If it has numbers and characters, increase strength value.
            if (password.match(/([a-zA-Z])/) && password.match(/([0-9])/)) strength += 1
// If it has one special character, increase strength value.
            if (password.match(/([-!$%^&*()_+|~=`{}\[\]:";'<>?,.\/])/)) { strength += 1;}
// If it has two special characters, increase strength value.
            if (password.match(/(.*[-!$%^&*()_+|~=`{}\[\]:";'<>?,.\/].*[-!$%^&*()_+|~=`{}\[\]:";'<>?,.\/])/)) {strength += 1;}

// Calculated strength value, we can return messages
// If value is less than 2
            if (strength < 2) {
                $('#result').removeClass()
                $('#result').addClass('weak')
                return 'Weak'
            } else if (strength == 2) {
                $('#result').removeClass()
                $('#result').addClass('good')
                return 'Good'
            } else {
                $('#result').removeClass()
                $('#result').addClass('strong')
                return 'Strong'
            }
        }
    });
