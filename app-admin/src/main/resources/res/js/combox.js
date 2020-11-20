//联动框
(function($, window) {
	// 初始态定义
	var prefix ="combox_";
	var suffix = "_select";  //生成select Id 后缀
	
	// 插件定义
	$.fn.combox = function(_config) {
		// 默认参数，可被重写
		var defaults = {
			name : "",  //生成select name 
			textName:null,  //生成hidden input 用于记录select 选择后的text值
			initLoad:true, //是否加载初始数据
			url:null,  //加载数据url,当initLoad && url != null 才初始加载
			parentId:"",  //关联上级id
			valueField:"id",  //select value json对应字段
			textField:"text",  //select text json对应字段
			cssClass:"form-control",  //select css样式
			defaultName:"--请选择--",  //默认显示
			defaultValue:"",  //默认值
			changeUrl:null, //更改时url 写法: localhost:8080/../...do?key
		};

		// 插件配置
		this.oConfig = $.extend(defaults, _config);
		
		var _oSelf = this,
		$this = $(this),
		_parentId = _oSelf.oConfig.parentId+suffix,
		_selectId=$this.attr("id")+suffix,
		$parent = $("#"+_parentId),
		_defaultOption="<option value='"+_oSelf.oConfig.defaultValue+"'>"+_oSelf.oConfig.defaultName+"</option>";
		
		// 初始化函数
		var _init = function() {
			// 初始化数据
			_initData();
			// 事件绑定
			_loadEvent();
		};
		
		//初始化数据
		var _initData = function() {
			$this.append("<select name='"+_oSelf.oConfig.name+"' id='"+_selectId+"' class='"+_oSelf.oConfig.cssClass+"' >");
			if(_oSelf.oConfig.textName){
				$this.append("<input type='hidden' name='"+_oSelf.oConfig.textName+"' id='"+$this.attr("id")+"_hidden'  class='"+_oSelf.oConfig.cssClass+"'>");
			}
//			if(_oSelf.oConfig.isDefault){ //添加默认
				$("#"+_selectId).append(_defaultOption);
//			};
			if($parent.size() > 0){
				$("#"+_selectId).attr(prefix+"parent",_parentId);
				$("#"+$this.attr("id")+"_hidden").attr(prefix+"parent",_parentId);
			};
			if(_oSelf.oConfig.initLoad && _oSelf.oConfig.url != null){ //远程获取数据
				$.ajaxSettings.async = false; //设置ajax同步
				$.post(_oSelf.oConfig.url,function(data){
					$("#"+_selectId).append(_createOption(data));
				},"json");
				$.ajaxSettings.async = true; //设置ajax同步
			};

		};
		
		//事件绑定
		var _loadEvent = function() {
			$this.on("change","#"+_selectId,function(){//获取所有子节点  
				if($("#"+_selectId).val() !=  _oSelf.oConfig.defaultValue){
					$("#"+$this.attr("id")+"_hidden").val($("#"+_selectId).find("option:selected").text());
				}else{
					$("#"+$this.attr("id")+"_hidden").val("");
				}
				_recursion(_selectId);
				if(_oSelf.oConfig.changeUrl){
					$.ajaxSettings.async = false; //设置ajax同步
					$.post(_oSelf.oConfig.changeUrl+"="+$("#"+_selectId).val(),function(data){
						 $("select["+prefix+"parent="+_selectId+"]").append(_createOption(data));
					},"json");
					$.ajaxSettings.async = true; //设置ajax同步
				}
			});
		};
		
		//生成option
		var _createOption = function(data) {
			var options = "";
			$.each(data,function(n,value){
				options+="<option value='"+value[_oSelf.oConfig.valueField]+"'>"+value[_oSelf.oConfig.textField]+"</option>";
			});
			return options;
		};
		
		//递归处理子类
		var _recursion = function(parentId){
			if(parentId === undefined){
				return false;
			}
			var obj = $("select["+prefix+"parent="+parentId+"]");
			if(obj){
				obj.html("");
				if($("input["+prefix+"parent="+parentId+"]").size() > 0){
					$("input["+prefix+"parent="+parentId+"]").val("");
				}
				obj.append(_defaultOption);
				_recursion(obj.attr("id"));
			}else{
				return false;
			}
		};
		
		// 启动插件
		_init();

		// 链式调用
		return this;
	};
	// 插件结束
})(jQuery, window);