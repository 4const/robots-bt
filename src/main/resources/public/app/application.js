$(function() {
	__readSystemState = function(cb) {
		$.ajax({
			type: 'GET',
			url: '/state',
			async: false,
			success: cb
		});
	}

    __readSystemState(function(res) {
    	$('#state').text(JSON.stringify(res));
    });
});