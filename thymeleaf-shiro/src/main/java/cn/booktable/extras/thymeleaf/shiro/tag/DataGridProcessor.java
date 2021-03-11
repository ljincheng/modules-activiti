package cn.booktable.extras.thymeleaf.shiro.tag;

import cn.booktable.extras.thymeleaf.shiro.processor.ShiroFacade;
import org.thymeleaf.context.ITemplateContext;
import org.thymeleaf.engine.AttributeName;
import org.thymeleaf.exceptions.TemplateProcessingException;
import org.thymeleaf.model.IModel;
import org.thymeleaf.model.IModelFactory;
import org.thymeleaf.model.IOpenElementTag;
import org.thymeleaf.model.IProcessableElementTag;
import org.thymeleaf.processor.element.AbstractAttributeTagProcessor;
import org.thymeleaf.processor.element.AbstractElementTagProcessor;
import org.thymeleaf.processor.element.IElementTagStructureHandler;
import org.thymeleaf.standard.expression.Each;
import org.thymeleaf.standard.expression.EachUtils;
import org.thymeleaf.standard.expression.IStandardExpression;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.util.FastStringWriter;
import org.thymeleaf.util.StringUtils;
import org.unbescape.html.HtmlEscape;

/**
 * @author ljc
 */
public class DataGridProcessor extends AbstractElementTagProcessor {
    private static final String ELEMENT_NAME = "dataGrid";

    private static final int PRECEDENCE = 300;

//    public DataGridProcessor(final TemplateMode templateMode, final String dialectPrefix) {
//        super(templateMode, dialectPrefix, null, false, ATTR_NAME, true, PRECEDENCE, true);
//    }
public DataGridProcessor(String dialectPrefix) {
    super(
            TemplateMode.HTML, // This processor will apply only to HTML mode
            dialectPrefix, // Prefix to be applied to name for matching
            ELEMENT_NAME, // Tag name: match specifically this tag
            true, // Apply dialect prefix to tag name
            null, // No attribute name: will match by tag name
            false, // No prefix to be applied to attribute name
            PRECEDENCE); // Precedence (inside dialect's own precedence)
}



    @Override
    protected void doProcess(ITemplateContext iTemplateContext,
                             IProcessableElementTag iProcessableElementTag,
                             IElementTagStructureHandler iElementTagStructureHandler) {
        final String url = iProcessableElementTag.getAttributeValue("url");
        final String id = iProcessableElementTag.getAttributeValue("id");
/*
        <input type="hidden" name="pageIndex" value="1" id="pageIndex">
        <input type="hidden" name="pageSize" value="20">
        <input type="hidden" id="viewType" name="viewType" value="1">
    */
//         IModelFactory modelFactory= iTemplateContext.getModelFactory();
//        final IModel model = modelFactory.createModel();
//        model.add(modelFactory.createOpenElementTag("form"));
//        model.add(modelFactory.createText("<input type=\"hidden\" name=\"url\" value=\""+url+"\" />"));
//        model.add(modelFactory.createText("<input type=\"hidden\" name=\"pageIndex\" id=\"pageIndex\" value=\"1\" />"));
//        model.add(modelFactory.createText("<input type=\"hidden\" name=\"pageSize\" value=\"20\" />"));
//        model.add(modelFactory.createCloseElementTag("form"));


        final FastStringWriter writer = new FastStringWriter(200);
        writer.write("<form id=\""+id+"\" action=\""+url+"\">");
        writer.write("<input type=\"hidden\" name=\"pageIndex\" id=\"pageIndex\" value=\"1\" />");
        writer.write("<input type=\"hidden\" name=\"pageSize\" value=\"20\" />");
        writer.write("</form>");

//        iElementTagStructureHandler.replaceWith(model, false);
        iElementTagStructureHandler.replaceWith(writer.toString(),false);
    }
}