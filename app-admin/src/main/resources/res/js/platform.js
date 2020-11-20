/**
 * 
 */

//初始化平台框架
 
	$(function(){
		var h_page=$(window).height();
		var w_page=$(window).width();
		var h_pageHeader=0;
		var w_pageleft=202;
		var h_pageleft=h_page-h_pageHeader;
		var w_main=w_page-w_pageleft;
		var h_main=h_page-h_pageHeader;
		$("div.page-left").css("height",h_pageleft);
		$("div.page-header").css("width",w_page);
		$("div.page-header-container").css("width",w_page);
//	 	$("div.page-header").css("height",62); 
		$("div.page-main").css("width",w_main);
		$("div.page-main").css("height",h_main);
		$("div.pagecontainer").css("width",w_page);
		$("div.pagecontainer").css("height",h_page);
		$("#mainFrame").css("width",w_main);
		$("#mainFrame").css("height",h_main);
		afterPageLoad();
	});	
 
function openmenu(url)
{
	if(url)
		{
		$("#mainFrame").attr("src",url);
		}
}
 


function afterPageLoad() {
  $('#pageBody .menu').menu();
  $('#pageBody .menu .nav li:not(".nav-parent") a').click(function() {
      var $this = $(this);
      openmenu($this.attr("eventdata"));
      $('.menu .nav .active').removeClass('active');
      $this.closest('li').addClass('active');
      var parent = $this.closest('.nav-parent');
      if(parent.length)
      {
          parent.addClass('active');
      }
  });
}