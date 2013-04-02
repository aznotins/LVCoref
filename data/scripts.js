$(function(){

	$.fn.clickToggle = function(func1, func2) {
		var funcs = [func1, func2];
		this.data('toggleclicked', 0);
		this.click(function() {
		    var data = $(this).data();
		    var tc = data.toggleclicked;
		    $.proxy(funcs[tc], this)();
		    data.toggleclicked = (tc + 1) % 2;
		});
		return this;
	    };

	$('span').clickToggle(
		function(){
			$('.'+$(this).attr('class')).css('font-weight','bold').css('font-size','25px');
console.log('te');
		
		},
		function(){
			$('.'+$(this).attr('class')).css('font-weight','normal').css('font-size','16px');		
		}
	)
})


function randomColors(total)
{
    var i = 360 / (total - 1); // distribute the colors evenly on the hue range
    var r = []; // hold the generated colors
    for (var x=0; x<total; x++)
    {
        r.push(hsvToRgb(i * x, 100, 100)); // you can also alternate the saturation and value for even more contrast between the colors
    }
    return r;
}


	/*$('span').hover(
		function(){
			$('.'+$(this).attr('class')).css('font-weight','bold').css('font-size','20px');
		},
		function(){
			$('.'+$(this).attr('class')).css('font-weight','normal').css('font-size','16px');		
	})*/

	/*$('span').toggle(
		function(){
			//$('.'+$(this).attr('class')).css('font-weight','bold').css('font-size','20px');
console.log('te');
		
		},
		function(){
			//$('.'+$(this).attr('class')).css('font-weight','normal').css('font-size','16px');		
		}
	)*/
