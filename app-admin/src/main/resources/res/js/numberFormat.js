/**
 * 转义数字包含千位符 如 1000.format(2)=1000.00 10000.format(2)=1,000.00
 * @param n 为保留小数位
 * @returns {String}
 */
Number.prototype.format = function(n) {
	var s = this;
	n = n >= 0 && n <= 20 ? n : 2; 
    s = parseFloat((s + "").replace(/[^\d\.-]/g, "")).toFixed(n) + "";         
	var l = s.split(".")[0].split("").reverse(),   
	r = s.split(".")[1]; 	
	t = ""; 
    for(var i = 0; i < l.length; i ++ )   
    {   
	  t += l[i] + ((i + 1) % 3 == 0 && (i + 1) != l.length ? "," : "");   
    }   
	if(n===0){
		return t.split("").reverse().join("") ;
	}
	return t.split("").reverse().join("") + "." + r;
};