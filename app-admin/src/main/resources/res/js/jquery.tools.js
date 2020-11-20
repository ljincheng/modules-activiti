window.getNowFormatDate = function () {
    var date = new Date();
    var seperator1 = "-";
    var year = date.getFullYear();
    var month = date.getMonth() + 1;
    var strDate = date.getDate();
    if (month >= 1 && month <= 9) {
        month = "0" + month;
    }
    if (strDate >= 0 && strDate <= 9) {
        strDate = "0" + strDate;
    }
    var currentdate = year + seperator1 + month + seperator1 + strDate;
    return currentdate;
}

window.alertLayer = function (msg, opt) {
    var opts = $.extend({end: null, icon: -1,title:"", id: null,}, opt);
//	layer.alert({dialog: { msg:msg,type:opts.type},border:[0],end:opts.end});
    //  if (opts.id != null) {
    switch (opts.icon) {
        case -1:
            swal(msg, {
                buttons: false,
                timer: 3000,
            });
            break;
        // case 0:
        //     $("#" + opts.id).html("<div class=\"alert alert-block alert-info alert-dismissable with-icon\">   <i class=\"icon-info-sign\"></i><div class=\"content\">" + msg + "</div><button type=\"button\" class=\"close\" data-dismiss=\"alert\" aria-hidden=\"true\">×</button></div>");
        //     break;
        case 1:
            swal(opts.title, msg, {
                icon : "warning",
                buttons: {
                    confirm: {
                        className : 'btn btn-warning'
                    }
                },
            });

            break;
        case 2:
            swal(opts.title, msg, {
                icon : "error",
                buttons: {
                    confirm: {
                        className : 'btn btn-danger'
                    }
                },
            });

            break;
        default:
            swal(opts.title, msg, {
                icon : "info",
                buttons: {
                    confirm: {
                        className : 'btn btn-info'
                    }
                },
            });
            break;
    }
}

/**
 * 弹出confirm,用于询问用户是否确认当前操作等,只能显示两个按钮
 * 输入参数格式:{msg:"",yesTitle:'确定',yesFn:null,noTitle:'取消',noFn:null}
 * msg:询问内容
 * yesTitle:第一个按钮名称,默认确定
 * yesFn:第一个按钮回调事件
 * noTitle:第二个按钮名称,默认取消
 * noFn:第二个按钮回调事件
 */
window.confirmLayer = function (opt) {
    var opts = $.extend({msg: "", yesTitle: '确定', yesFn: null, noTitle: '取消', noFn: null,title:''}, opt);

    swal({
        title: opt.title,
        text: opt.msg,
        type: 'warning',
        buttons:{
            confirm: {
                text : opts.yesTitle,
                className : 'btn btn-success'
            },
            cancel: {
                visible: true,
                text : opts.noTitle,
                className: 'btn btn-danger'
            }
        }
    }).then((Delete) => {
        if (Delete) {
            if (opts.yesFn != null) {
                opts.yesFn();
            }
        } else {
            swal.close();
        }
    });
}

/**
 * 弹出prompt,用于备注，审批等,只能显示一个输入框和两个按钮
 * @param opt:{yesBt:"确定",noBt:"取消",title:"提示",formType:2,value:"",maxlength:100,iscancel:true,isRequired:true,yesFn:null,noFn:null}
 * yesBt:第一个按钮名称,默认确定
 * noBt:第二个按钮名称,默认取消
 * title:页头名称,默认提示
 * formType:输入框类型 1 password 2 textare 其他 text  默认2
 * value:输入框默认值,默认空
 * maxlength:输入框字数限制,默认为100
 * iscancel:是否屏蔽输入不正确时第二个按钮默认事件(关闭对话框), 默认 true 不屏蔽
 * isRequired:确认按钮时是否必须输入内容 默认 true 必须
 * yesFn:第一个按钮回调事件
 * noFn:第二个按钮回调事件
 */
window.promptLayer = function (opt) {
    var opts = $.extend({
        yesBt: "确定",
        noBt: "取消",
        title: "提示",
        formType: 2,
        value: "",
        maxlength: 100,
        iscancel: true,
        isRequired: true,
        yesFn: null,
        noFn: null
    }, opt);
    var prompt = null;
    var content = null;
    switch (opts.formType) {
        case 1:{
            prompt = '<input type="password" class="layui-layer-input" value="' + (opts.value || '') + '">';
            content = prompt;
        }
            break;
        case 2:{
            prompt = '<textarea class="layui-layer-input">' + (opts.value || '') + '</textarea>';
            content = prompt;
        }
            break;
        case 3:{
            prompt = '<input type="text" class="layui-layer-input" value="' + (opts.value || '') + '">';
            content = prompt;
        }
            break;
        case 4:{
            prompt = '<input class="layui-layer-input" type="text"  onclick="WdatePicker({dateFmt:\'yyyy-MM-dd\',readOnly:\'readOnly\'})" value="' + (opts.value || getNowFormatDate()) + '" >';
            content = prompt;
        }

            break;
        case 5:

            prompt = '<input type="text" class="layui-layer-input numText" value="' + (opts.value || '') + '">';
            content = prompt;

            break;
        default:
            break;
    }

    layer.open({
        title: opts.title,
        btn: [opts.yesBt, opts.noBt],
        content: content,
        skin: 'layui-layer-prompt',
        success: function (layero) {
            prompt = layero.find('.layui-layer-input');
            prompt.focus();
            numInputFn();
        },
        btn1: function (index) {
            var value = prompt.val();
            if (opts.isRequired) {
                if (value === '') {
                    prompt.focus();
                    layer.tips('内容不能为空!', prompt, {tips: 1, time: 500});
                } else if (value.length > (opts.maxlength || 100)) {
                    layer.tips('最多输入' + (opts.maxlength || 100) + '个字数', prompt, {tips: 1, time: 500});
                } else {
                    if (opts.yesFn != null) {
                        opts.yesFn(value, index, prompt);
                    }
                }
            } else {
                if (opts.yesFn != null) {
                    opts.yesFn(value, index, prompt);
                }
            }
        },
        btn2: function (index) {
            var value = prompt.val();
            if (opts.iscancel) {
                if (opts.noFn != null) {
                    opts.noFn(value, index, prompt);
                }
            } else {
                if (value === '') {
                    prompt.focus();
                    layer.tips('内容不能为空!', prompt, {tips: 1, time: 500});
                    return opts.iscancel;
                } else if (value.length > (opts.maxlength || 100)) {
                    layer.tips('最多输入' + (opts.maxlength || 100) + '个字数', prompt, {tips: 1, time: 500});
                    return opts.iscancel;
                } else {
                    if (opts.noFn != null) {
                        opts.noFn(value, index, prompt);
                    }
                }
            }
        },
    });
};

window.TipsLayer = function (opt) {
    var opts = $.extend({msg: "", obj: ""}, opt);
    if (obj != "") {
        layer.tips(opts.msg, opts.obj);
    }
}


window.openWindow = function (opt) {
    var w = window.innerWidth - 60;
    var h = window.innerHeight - 60;
    var offset_t=30;
    var offset_l=30;
    // var opts = $.extend({
    //     title: "&nbsp;&nbsp;", type: 2, url: null, width: w, height: h, complete:null,close: function (index) {
    //         layer.close(index);
    //     }
    // }, opt);
    var opts = $.extend({
        title: "&nbsp;&nbsp;", type: 2, url: null, width: w, height: h, complete:null}, opt);

    function myWindowClose(index)
    {
        if(opts.complete!=null)
        {
            opts.complete();
        }
        layer.close(index);
    }

    if (opts.width > w) {
        opts.width = w;
    }
    var p_w=w-opts.width;
    if(p_w!=0)
    {
        offset_l=Math.round(p_w/2);
    }


    if (opts.height > h) {
        opts.height = h;
    }
    var p_h=(h-opts.height);
    if(p_h!=0)
    {
        offset_t=Math.round(p_h/2);
    }
    var layerindex = layer.open({
        type: opts.type,
        title: opts.title,
        area: [opts.width + 'px', opts.height + 'px'],
        offset:[offset_t+"px",offset_l+"px"],
        fix: true,
//	    offset: [($(window).height() - opts.height)/2+'px', ''],
        content: opts.url,
        end:myWindowClose
    });
    window.openwindowLastIndex = layerindex;
    return layerindex;
}

/**
 * 关闭弹出层
 */
window.closeWindow = function (index) {
    var winIndex = index || window.openwindowLastIndex;
    layer.close(winIndex);
}
/**
 * 显示加载中
 */
window.loading_Layer_Index = null;

function loading_start() {
    window.loading_Layer_Index = layer.load(0, {shade: [0.3, '#393D49']});
}

/**
 * 关闭加载中
 */
function loading_close() {
    if (window.loading_Layer_Index != null) {
        window.setTimeout(function () {
//				layer.loadClose();
            layer.close(window.loading_Layer_Index);
            window.loading_Layer_Index = null;
        }, 500);
    }
}

/**
 *
 * @param opt
 */
function loadTableData(opt) {
    var opts = $.extend({id: null, url: null, complete: null, showLoading: true, data: null,type:"POST",cache:false, dataType:"html",async:true}, opt);
    if (opts.id != null) {
        $.ajax({
            type: opts.type,
            url: opts.url,
            data:opts.data,
            cache: opts.cache,
            dataType:opts.dataType,
            async:opts.async,
            context:opts,
            beforeSend:function(XHR){ if (opts.showLoading) {
                loading_start();
            }},
            success: function(htmldata){
                if (this.showLoading) {
                    loading_close();
                }
                if (this.complete != null) {
                    this.complete(htmldata);
                }
                $("#" + this.id).html(htmldata);
            },error:function(XMLHttpRequest, textStatus, errorThrown) { alertLayer("请求失败："+textStatus,{icon: 5}); }
            ,complete:function() { loading_close();}
        });
    }
}

function loadJsonData(opt) {
    var opts = $.extend({url: null, complete: null,type:"POST",cache:false, dataType:"json", showLoading: true, data: null,async:true}, opt);
    if (opts.url != null) {
        $.ajax({
            type: opts.type,
            url: opts.url,
            data:opts.data,
            cache: opts.cache,
            dataType:opts.dataType,
            async:opts.async,
            context:opts,
            beforeSend:function(XHR){ if (opts.showLoading) {
                loading_start();
            }},
            success: function(msg){
                if (this.showLoading) {
                    loading_close();
                }
                if (this.complete != null) {
                    this.complete(msg,this);
                }
            },error:function(XMLHttpRequest, textStatus, errorThrown) { alertLayer("请求失败："+textStatus,{icon: 5}); }
            ,complete:function() { loading_close();}
        });
    }
}

function showJsonData(data) {
    var jsonResult = $.extend({code: "", msg: "", isIframe: false}, data);
    openAlert({id: "promptMsg", code: jsonResult.code, msg: jsonResult.msg, isIframe: jsonResult.isIframe});
}

window.openHtmlWindow = function (opt, html) {
    var w = $(window).width() - 60;
    var h = $(window).height() - 60;
    var opts = $.extend({
        title: "&nbsp;&nbsp;", type: 2, url: null, width: w, height: h, close: function (index) {
            layer.close(index);
        }
    }, opt);
    if (opts.width > w) {
        opts.width = w;
    }
    if (opts.height > h) {
        opts.height = h;
    }
    var layerindex = layer.open({
        type: opts.type,
        title: opts.title,
        area: [opts.width + 'px', opts.height + 'px'],
        fix: true,
        content: html,
        end: opts.close
    });
    window.openwindowLastIndex = layerindex;
    return layerindex;
}


function openAlert(opt) {
    var opts = $.extend({id: "promptMsg", code: null, msg: null, delay: 5000, isIframe: false}, opt);
    if (opts.code != null && opts.msg != null) {
        if (opts.isIframe) {
            $("#" + opts.id, window.parent.document).html(
                '<div class="fade in alert ' + (opts.code == 1 ? 'alert-success' : 'alert-danger') + '">' +
                '<a href="#" class="close" data-dismiss="alert">&times;</a>' + opts.msg +
                '</div>');
        } else {
            $("#" + opts.id).html(
                '<div class="fade in alert ' + (opts.code == 1 ? 'alert-success' : 'alert-danger') + '">' +
                '<a href="#" class="close" data-dismiss="alert">&times;</a>' + opts.msg +
                '</div>');
        }

        setTimeout(closeAlert, opts.delay);
    }
}

function closeAlert() {
    $(".alert", window.parent.document).alert('close');
    $(".alert").alert('close');
}


/**
 * 分页
 * @param opt
 */
function jqPager(opt) {
    var opts = $.extend({navObj:"ul.pager",navCountObj:".pager_totalnum", totalPages: 0, pageSize: 10, pageIndex: 1, change: null,firstBt:"首页",preBt:"上一页",nextBt:"下一页",lastBt:"末页",totalNum:null}, opt);
    if (opts.totalPages > 0) {
        $(opts.navObj).jqPaginator({
            totalPages: opts.totalPages,
            visiblePages: opts.pageSize,
            currentPage: opts.pageIndex,
            first: '<li class="first page-item"><a href="javascript:void(0);" class="page-link">'+opts.firstBt+'<\/a><\/li>',
            prev: '<li class="previous page-item"><a href="javascript:void(0);" class="page-link"><i class="arrow arrow2"><\/i>'+opts.preBt+'<\/a><\/li>',
            next: '<li class="next page-item"><a href="javascript:void(0);" class="page-link">'+opts.nextBt+'<i class="arrow arrow3"><\/i><\/a><\/li>',
            last: '<li class="last page-item"><a href="javascript:void(0);" class="page-link">'+opts.lastBt+'<\/a><\/li>',
            page: '<li class="page page-item"><a href="javascript:void(0);" class="page-link">{{page}}<\/a><\/li>',
            onPageChange: function (num, type) {
                if (type == "change") {
                    if (opts.change != null) {
                        opts.change(num);
                    }
                }

            }
        });

     }else{
        $(opts.navObj).jqPaginator({
            totalPages: 1,
            visiblePages: opts.pageSize,
            currentPage: 1,
            first: '<li class="first page-item"><a href="javascript:void(0);" class="page-link">'+opts.firstBt+'<\/a><\/li>',
            prev: '<li class="previous page-item"><a href="javascript:void(0);" class="page-link"><i class="arrow arrow2"><\/i>'+opts.preBt+'<\/a><\/li>',
            next: '<li class="next page-item"><a href="javascript:void(0);" class="page-link">'+opts.nextBt+'<i class="arrow arrow3"><\/i><\/a><\/li>',
            last: '<li class="last page-item"><a href="javascript:void(0);" class="page-link">'+opts.lastBt+'<\/a><\/li>',
            page: '<li class="page page-item"><a href="javascript:void(0);" class="page-link">{{page}}<\/a><\/li>',
            onPageChange: function (num, type) {
                if (type == "change") {
                    if (opts.change != null) {
                        opts.change(num);
                    }
                }

            }
        });
    }

    if($(opts.navCountObj).length > 0 && opts.totalNum!=null){
        $(opts.navCountObj).text(opts.totalNum);
    }
}


/**
 * 生成uuid
 * @returns
 */
function generateUUID() {
    var d = new Date().getTime();
    var uuid = 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function (c) {
        var r = (d + Math.random() * 16) % 16 | 0;
        d = Math.floor(d / 16);
        return (c == 'x' ? r : (r & 0x3 | 0x8)).toString(16);
    });
    return uuid;
};

Math.formatFloat = function (f, digit) {
    var m = Math.pow(10, digit);
    return Math.round(f * m, 10) / m;
}

function numInputFn()
{
    /*JQuery 限制文本框只能输入数字*/
    $(".numText").keyup(function(){
        //alert("numInputFn, keyup");
        //$(this).val($(this).val().replace(/D|^0/g,''));
        //$(this).attr('placeholder',"只能输入数字");
        var newLength=$(this).val().replace(/[^\d]/g, "").length;
        if(newLength!=$(this).val().length)
        {
            $(this).val($(this).val().replace(/[^\d]/g, ""));
            // alert("请输入正确的数字!");
            return false;
        }
        return true;
    }).bind("paste",function(){  //CTR+V事件处理
        $(this).val($(this).val().replace(/D|^0/g,''));
    }).css("ime-mode", "disabled"); //CSS设置输入法不可用

    /*JQuery 限制文本框只能输入数字和小数点*/
    $(".numDecText").keyup(function(){
        $(this).val($(this).val().replace(/[^0-9.]/g,''));
    }).bind("paste",function(){
        $(this).val($(this).val().replace(/[^0-9.]/g,'')); //粘贴的不是数字，则替换为''
    }).css("ime-mode", "disabled"); //CSS设置输入法不可用

}
$(function (){
    numInputFn();
});
