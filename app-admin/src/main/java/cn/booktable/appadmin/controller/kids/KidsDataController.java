package cn.booktable.appadmin.controller.kids;

import cn.booktable.core.view.JsonView;
import cn.booktable.modules.elasticsearch.bean.ElasticsearchBo;
import cn.booktable.modules.elasticsearch.service.ElasticsearchService;
import cn.booktable.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * @author ljc
 */
@Controller
@RequestMapping("/kids")
public class KidsDataController {

    private static Logger logger= LoggerFactory.getLogger(KidsDataController.class);

    @Autowired
    private ElasticsearchService elasticsearchService;

    @GetMapping(value="/test")
    public JsonView<Map> test(HttpServletRequest request){
        JsonView<Map> view=new JsonView<>();
        try{
            Map<String,Object> result=new HashMap<>();
            Map<String, Object> paramMap = new HashMap<>();
            Enumeration<String> keyNames = request.getParameterNames();
            if(StringUtils.isEmpty(request.getParameter("pageIndex"))){
                paramMap.put("pageIndex", "1");
            }
            if(StringUtils.isEmpty(request.getParameter("pageSize"))){
                paramMap.put("pageSize","20");
            }
            while (keyNames.hasMoreElements()) {
                String attrName = keyNames.nextElement();
                String attrValue = request.getParameter(attrName);
                logger.info("KEY: {}={}",attrName,attrValue);
            }

            ElasticsearchBo elasticsearchBo= elasticsearchService.info();
            logger.info("el="+elasticsearchBo);
            result.put("elasticsearch",elasticsearchBo);
            result.put("param",paramMap);
            view.setData(result);
        }catch (Exception ex){
            logger.error("TEST:",ex);
        }
        return view;
    }

}
