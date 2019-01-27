function format ( d ) {
    return d.Content;
}

$(document).ready(function() {
	var table =  $('#myTable').DataTable( {
		"bProcessing": true,						  
		"order": [[ 1, "asc" ],  [2,"asc"] ],		
		"pageLength": -1,						
		"columnDefs": [{				
            "targets": [ 5 ],		// hide fifth column
            "visible": false
        }],
		"bPaginate": false,
		"bLengthChange": false,
		"columns": [
            {							// first column: button
                "className":      'details-control',
                "orderable":      false,
                "data":           null,
                "defaultContent": ''
            },
            { "data": "Titel" },		// second column
            { "data": "Original Titel" },
            { "data": "Year" },
            { "data": "Genre" },
            { "data": "Content" }		// content: hidden
        ]
	} );
	
    // Setup - add a text input to each footer cell
    $('#myTable tfoot th').not(":eq(0)").each( function () {	// search bar for each column but first
        var title = $(this).text();
        $(this).html( '<input type="text" placeholder="Search '+title+'" />' );
    } );
 	
	// Add event listener for opening and closing details
    $('#myTable tbody').on('click', 'td.details-control', function () {
        var tr = $(this).closest('tr');
        var row = table.row( tr );
 
        if ( row.child.isShown() ) {
            // This row is already open - close it
            row.child.hide();
            tr.removeClass('shown');
        }
        else {
            // Open this row
            row.child( format(row.data()) ).show();
            tr.addClass('shown');
        }
    } );
	
    // Apply the search
    table.columns().every( function () {
        var that = this; 
        $( 'input', this.footer() ).on( 'keyup change', function () {
            if ( that.search() !== this.value ) {
                that
                    .search( this.value )
                    .draw();
            }
        } );
    } );
});
