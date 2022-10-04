$( "#validateAge" ).click(function() {
    var firstName=$("#firstName").val();
    var lastName=$("#lastName").val();
    var age=$("#age").val();
  $.ajax({
        url: '/bin/saveUserDetails',
        type: 'POST',
      data: { "firstName":firstName, "lastName" :lastName, "age" :age} ,

        success: function (data) {
			$( "#form-error-msg" ).remove();
            $("<p id='form-sucess-msg'>Thank you the form has been submitted</p>").insertAfter("#validateAge");
        },
        error: function () {
            $( "#form-sucess-msg" ).remove();
              $("<p  id='form-error-msg'>Please enter the Age between 18 to 50 years</p>").insertAfter("#validateAge");
        }
    }); 
});