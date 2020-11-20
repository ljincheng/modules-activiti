package cn.booktable.appadmin.controller.elasticsearch;

import cn.booktable.core.page.PageDo;
import cn.booktable.core.view.JsonView;
import cn.booktable.modules.elasticsearch.bean.ElasticsearchBo;
import cn.booktable.modules.elasticsearch.bean.EsSearchItemBo;
import cn.booktable.modules.elasticsearch.bean.ProductBo;
import cn.booktable.modules.elasticsearch.service.elasticsearch.EsSearchService;
import cn.booktable.modules.entity.kids.KidsMediaMetadataDo;
import cn.booktable.modules.service.kids.KidsElasticsearchService;
import cn.booktable.appadmin.utils.ViewUtils;
import cn.booktable.util.StringUtils;
import com.alibaba.fastjson.JSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/es")
public class ElasticsearchController {

    private static Logger logger=LoggerFactory.getLogger(ElasticsearchController.class);
    @Autowired
    private MessageSource messageSource;
    @Autowired
    private KidsElasticsearchService kidsElasticsearchService;

    @GetMapping("/query")
    public ModelAndView list(){
        ModelAndView view=new ModelAndView("elasticsearch/query");
        return view;
    }

    @GetMapping("/indexInit")
    public JsonView<String> indexInit(){
        JsonView view=new JsonView();
        try{
            kidsElasticsearchService.indexInit();
        }catch (Exception ex)
        {
            ViewUtils.pushException(view,messageSource,ex);
        }
        return view;
    }

    @GetMapping("/createIndex")
    public JsonView<String> createIndex(){
        JsonView view=new JsonView();
        try{
            kidsElasticsearchService.createIndex();
        }catch (Exception ex)
        {
            ViewUtils.pushException(view,messageSource,ex);
        }
        return view;
    }


    @PostMapping(value="/query")
    public ModelAndView query(HttpServletRequest request, String k, @RequestParam(required = false,defaultValue ="1")int pageIndex, @RequestParam(required = false,defaultValue ="20")int pageSize){

        ModelAndView view=new ModelAndView("elasticsearch/query_table");

        try{
            Map<String,Object> selected=new HashMap<>();
            if(StringUtils.isNotBlank(k)) {
                selected.put("brandName", k);
            }
            PageDo<KidsMediaMetadataDo> page= kidsElasticsearchService.query(k,pageIndex,20);
            logger.info("matchQuery Test:{}", JSON.toJSON(page));
            view.addObject("pagedata",page);
            ViewUtils.submitSuccess(view,messageSource);
        }catch (Exception ex){
            logger.error("TEST:",ex);
        }
        return view;
    }

}
