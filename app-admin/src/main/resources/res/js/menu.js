var main_tabs=[];
function openmenu(url,title)
{
	if(url)
		{
//		$("#mainNavTabs").append("  <li><a>"+title+"</a></li>");
//		$("#mainFrame").att|r("src",url);
		if (!/mobile|Android|webOS|iPhone|iPad|iPod|BlackBerry|BB|PlayBook|IEMobile|Windows Phone|Kindle|Silk|Opera Mini/i.test(navigator.userAgent)) {
			addTabs(url,title,true);
			setMainFrameSize();
		}else{
			window.open(url,title);
		}
		
		}
}
function closeTab(event)
{
	event.data.tabdiv.remove();
	event.data.tab.remove();
	var next_main_tab=null;
	for(var i=0,k=main_tabs.length;i<k;i++)
	{
		var main_tab=main_tabs[i];
		if(main_tab==event.data)
		{
			main_tabs.splice(i,1);
			break;
		}else{
			next_main_tab=main_tab;
		} 
	}
	if(next_main_tab==null && main_tabs.length>0)
		{
		next_main_tab=main_tabs[0];
		}
	if(next_main_tab!=null)
		{
			//openmenu(next_main_tab.url,next_main_tab.title);
			addTabs(next_main_tab.url,next_main_tab.title,false);
		}
	
}
function resizeTab()
{
	var pH=$("#mainFrameWrapper").height();
	var pW=$("#mainFrameWrapper").width();
	for(var i=0,k=main_tabs.length;i<k;i++)
	{
		var main_tab=main_tabs[i];
		 main_tab.tabdiv.height(pH);
		 main_tab.tabdiv.width(pW);
	}
}
function addTabs(url,title,beref)
{
	var thisTab=null;
	for(var i=0,k=main_tabs.length;i<k;i++)
		{
			var main_tab=main_tabs[i];
			if(main_tab.title==title && main_tab.url==url)
			{
				thisTab=main_tab;
				main_tab.tabdiv.show();
				main_tab.tab.addClass("selected");
				if(beref)
					{
						main_tab.content.attr("src",url);
					}
			}else{
				// main_tab.tabdiv.hide();
				// main_tab.tab.removeClass("selected");
			}
		}
	if(thisTab==null)
		{
//			var tab=$('<li class="btn-link"></li>')
//			var tab_a=$("<a>"+title+"</a>");
//			var closeBtn=$("<span  style='margin-left:10px;display:none;' title='关闭'><i class='fa fa-window-close'></i></span>");
			var tab=$('<li/>',{'class':"btn-link"});
			var tab_a=$('<a/>').text(title);
			var closeBtn=$("<span/>",{'style':"margin-left:2px; position: relative;right:-4px; visibility: hidden",'title':"关闭"}).html("<i class='fa fa-times'></i>");
			tab.append(tab_a);
			tab_a.append(closeBtn);
			$("#mainNavTabs").append(tab);
			var pH=$("#mainFrameWrapper").height();
			var pW=$("#mainFrameWrapper").width();
			var content = $('<iframe class="mainFrame" scrolling="auto" frameborder="0" src="'+url+'" style="width:100%;height:100%; background:#ffffff;"></iframe>');
			var tabdiv=$('<div/>',{"style":"position: relative;left: 0px;top: 0px; width:100%,height:100%","class":"mainFrameDiv"});        
//			    tabdiv.addclass('active')
			    tabdiv.append(content);
			    tabdiv.height(pH);
			    tabdiv.width(pW);
			$("#mainFrameWrapper").append(tabdiv);
			thisTab={title:title,url:url,content:content,tabdiv:tabdiv,tab:tab};
			main_tabs.push(thisTab);
			tab.addClass("selected");
			function myHandler(event) {
				addTabs(event.data.url,event.data.title);
			 }
			tab.click(thisTab, myHandler)
			tab.on("mouseover", function(){
				var span=$(this).find("span").css("visibility","visible");
			
//			span.show();
			});
			tab.on("mouseout", function(){
//				$(this).find("span").hide();
				var span=$(this).find("span").css("visibility","hidden");
				});
			
			closeBtn.on("click",thisTab,closeTab);
		}

	for(var i=0,k=main_tabs.length;i<k;i++)
	{
		var main_tab=main_tabs[i];
		if(main_tab.title==title && main_tab.url==url)
		{
		}else{
			main_tab.tabdiv.hide();
			main_tab.tab.removeClass("selected");
		}
	}
	
}

var mainSidebarWidth=200;
var mainSidebar_index=1;
function  setMainFrameSize(index)
{
	  var window_height = $(window).height();
      var window_width = $(window).width();
      var sidebar_width = $(".sidebar").width();
      //var content_height = $(".content-wrapper").height();
      var content_height = $(".main-sidebar .sidebar").outerHeight();
      var mainWindowHeight=(window_height-$('.main-header').outerHeight());
      var mainWindowWidth=window_width-mainSidebarWidth;
      content_height=mainWindowHeight>content_height?mainWindowHeight:content_height;
      var frame_w=window_width;
      if(index)
    	  {
    	  	mainSidebar_index=index;
    	  }
      if(mainSidebar_index==1)
    	  {
    	  	  mainWindowWidth= window_width-mainSidebarWidth;
    	  }else if(mainSidebar_index==3){
    		  mainWindowWidth= window_width-40;
    	  }else if(mainSidebar_index==2){
    		  mainWindowWidth= window_width-mainSidebarWidth;
    	  } 
    
      var bodyHeight=content_height-43;
      $("#mainNavTabsPanel").css("width",mainWindowWidth+"px");
      $("#mainNavTabsPanel").css("height","40px");
      
      $(".mainFrame").css("width",mainWindowWidth+"px");
      $("#mainFrameWrapper").css("min-width",mainWindowWidth+"px");
      $("#mainFrameWrapper").css("height",bodyHeight+"px");	
      $("#mainFrameWrapper").css("min-height",bodyHeight+"px");	
      
      $(".mainFrameDiv").css("width",mainWindowWidth+"px");
      $(".mainFrameDiv").css("min-width",mainWindowWidth+"px");
      $(".mainFrameDiv").css("height",bodyHeight+"px");	
      $(".mainFrameDiv").css("min-height",bodyHeight+"px");	
     
      $(".mainFrame").css("width",mainWindowWidth+"px");
      $(".mainFrame").css("min-width",mainWindowWidth+"px");
      $(".mainFrame").css("height",bodyHeight+"px");	
      $(".mainFrame").css("min-height",bodyHeight+"px");	
    //  alert(mainWindowWidth+"|"+$(".mainFrameDiv").width()+"|"+$(".mainFrame").width()+"|"+$(".mainFrame").width());
      
    
      $(".content-wrapper").css("width",mainWindowWidth+"px");
      $(".content-wrapper").css("min-width",mainWindowWidth+"px");
      $(".content-wrapper").css("height",content_height+"px");	
      $(".content-wrapper").css("min-height",content_height+"px");	
      resizeTab();
//       	$("#mainFrame").css("width",frame_w+"px");
//	  	$("#mainFrame").css("min-width",frame_w+"px");
//  	$("#mainFrame").css("height",content_height+"px");	
//  	$("#mainFrame").css("min-height",content_height+"px");	
  	
}
$(function(){
	mainSidebarWidth=$(".main-sidebar").outerWidth();
	afterPageLoad();
	window.onresize = function(){  
		setMainFrameSize();
    }  

});

function afterPageLoad() {
 // $('.main-sidebar .menu').menu();
  $('#treeMenu').on('click', 'a', function() {
	    $('#treeMenu li.active').removeClass('active');
	    $(this).closest('li').addClass('active');
	});
  $('.main-sidebar .sidebar .sidebar-menu li:not(".treeview") a').click(function() {
      var $this = $(this);
      openmenu($this.attr("eventdata"),$this.text());
      $('.sidebar-menu .active').removeClass('active');
      $this.closest('li').addClass('active');
      var parent = $this.closest('.treeview');
      if(parent.length)
      {
          parent.addClass('active');
      }
  });
}