function debugCon(){
    var items=document.getElementsByTagName("a");
    if(items.length>0){
        for(var i=0,k=items.length;i<k;i++){
            var item=items[i];
            var href=item.getAttribute("href");
            console.log("href:"+href);
        }
    }
}