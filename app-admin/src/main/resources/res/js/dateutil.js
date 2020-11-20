
function getCurrentYear(){
	return getYear(new Date());
}

function getCurrentMonth(){
	return getMonth(new Date());
}

function getCurrentDay(){
	return getDay(new Date());
}
	
function getCurrentFirstDayOfMonth(){
	return getFirstDayOfMonth(new Date());
}

function getCurrentLastDayOfMonth(){
	return getLastDayOfMonth(new Date());
}

function getYear(dd){
	var date = new Date(dd);
	var year = date.getFullYear();
	return year;
}

function getMonth(dd){
	var date = new Date(dd);
	var mm = (date.getMonth()+1) < 10 ? "0"+(date.getMonth()+1) : (date.getMonth()+1);
	var month = date.getFullYear()+"-"+mm;//月份
	return month;
}

function getDay(dd){
	var date = new Date(dd);
	var mm = (date.getMonth()+1) < 10 ? "0"+(date.getMonth()+1) : (date.getMonth()+1);
	var dd = date.getDate() < 10 ? "0"+date.getDate() : date.getDate();
	var today = date.getFullYear()+"-"+mm+"-"+dd;//当天
	return today;
}

function getDay_(dd,n){
	var date = new Date(dd);
	date.setDate(date.getDate() + n);
	var mm = (date.getMonth()+1) < 10 ? "0"+(date.getMonth()+1) : (date.getMonth()+1);
	var dd = date.getDate() < 10 ? "0"+date.getDate() : date.getDate();
	var today = date.getFullYear()+"-"+mm+"-"+dd;//当天
	return today;
}

	
function getFirstDayOfMonth(dd){
	var date = new Date(dd);
	var mm = (date.getMonth()+1) < 10 ? "0"+(date.getMonth()+1) : (date.getMonth()+1);
	var firstDayOfMonth = date.getFullYear()+"-"+mm+"-"+"01";
	return firstDayOfMonth;
}

function getLastDayOfMonth(dd){
	var date = new Date(dd);
	var mm = (date.getMonth()+1) < 10 ? "0"+(date.getMonth()+1) : (date.getMonth()+1);
	var days = getDaysInMonth(date.getFullYear(),mm);
	var lastDayOfMonth = date.getFullYear()+"-"+mm+"-"+days;
	return lastDayOfMonth;
}

function getDaysInMonth(year,month){
    var temp = new Date(year,month,0);
    return temp.getDate();
}
	
