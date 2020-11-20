package cn.booktable.appadmin.controller.sys;

import cn.booktable.core.view.JsonView;
import cn.booktable.exception.BusinessException;
import cn.booktable.modules.entity.sys.SysAttachmentDo;
import cn.booktable.modules.service.sys.SysAttachmentService;
import cn.booktable.appadmin.config.AdminSysConfig;
import cn.booktable.appadmin.controller.base.BaseController;
import cn.booktable.toolkit.IdWorker;
import cn.booktable.util.AssertUtils;
import cn.booktable.util.ImageUtils;
import cn.booktable.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.HashMap;
import java.util.Map;

/**
 * 附件
 * @author ljc
 */
@Controller
@RequestMapping("/sys/attachment")
public class SysAttachmentController extends BaseController {

    private static Logger logger= LoggerFactory.getLogger(SysAttachmentController.class);

    //@Autowired
    @Autowired
    private SysAttachmentService sysAttachmentService;
    @Autowired
    private AdminSysConfig adminSysConfig;

    private static String PDF_ICON="pdf.png";
    private static String WORD_ICON="word.png";
    private static String EXCEL_ICON="excel.png";

    @RequestMapping(value="/upload",method= RequestMethod.POST)
    public JsonView<String> uploadAttachment_methodPost(@RequestParam(value = "file", required = false) MultipartFile file, HttpServletRequest request, String groupType)
    {
        JsonView<String> view=new JsonView<String>();
        try{
            AssertUtils.isNotBlank(groupType, "参数错误：[error:001]");
            AssertUtils.notNull(file,"上传图片文件不能为空");

            SysAttachmentDo attachment=new SysAttachmentDo();
            attachment.setCheckedStatus(SysAttachmentDo.CHECKEDSTATUS_INIT);

            String storeFileRootPath=adminSysConfig.getAttachmentRoot();
            String path=storeFileRootPath+groupType+"/";
            String saveFileName= IdWorker.getIdStr("D");
            int pointIndex=file.getOriginalFilename().lastIndexOf(".");
            String fileType=null;
            if(pointIndex>0)
            {
                saveFileName+=file.getOriginalFilename().substring( pointIndex);
                fileType=file.getOriginalFilename().substring( pointIndex+1);
            }

            File targetFile = new File(path, saveFileName);
            if(!targetFile.exists()){
                targetFile.mkdirs();
            }
            attachment.setFileName(file.getOriginalFilename());
            attachment.setCheckedStatus(SysAttachmentDo.CHECKEDSTATUS_INIT);
            attachment.setFilePath(path+saveFileName);
            attachment.setFileType(fileType);
            attachment.setGroupType(groupType);
            attachment.setCreateUserId(currentUser().getId());
            Integer dbRes=  sysAttachmentService.insertSysAttachment(attachment);
            AssertUtils.isTrue(dbRes!=null && dbRes>0,"保存失败");
            view.setData(attachment.getId().toString());
            //保存
            file.transferTo(targetFile);

            setPromptMessage(view, JsonView.CODE_SUCCESS,"保存成功");

        }catch(BusinessException e)
        {
            setPromptException(view, e);
        }catch (Exception e) {
            logger.error("上传Excel文件异常", e);
            setPromptException(view, e);
        }
        return view;
    }

    /**
     * 针对app上传图片
     * @param file
     * @param request
     * @param groupType
     * @return
     */
    @RequestMapping(value="/uploadkindEditorImg.do",method=RequestMethod.POST)
    @ResponseBody
    public Map<String,Object> app_kindEditorUploadAttachment_methodPost(@RequestParam(value = "file", required = false) MultipartFile file, HttpServletRequest request, String groupType)
    {
        Map<String,Object> view=new HashMap<>();
        try{
            AssertUtils.isNotBlank(groupType, "参数错误：[error:001]");
            AssertUtils.notNull(file,"上传图片文件不能为空");

            SysAttachmentDo attachment=new SysAttachmentDo();
            attachment.setCheckedStatus(SysAttachmentDo.CHECKEDSTATUS_INIT);

//			String storeFileRootPath=paramService.queryValueByCode("StoreFileRootPath");
            String storeFileRootPath=adminSysConfig.getAttachmentRoot();
            String path=storeFileRootPath+groupType+"/";
            String saveFileName=IdWorker.getIdStr("D");
            int pointIndex=file.getOriginalFilename().lastIndexOf(".");
            String fileType=null;
            if(pointIndex>0)
            {
                saveFileName+=file.getOriginalFilename().substring( pointIndex);
                fileType=file.getOriginalFilename().substring( pointIndex+1);
            }

            File targetFile = new File(path, saveFileName);
            if(!targetFile.exists()){
                targetFile.mkdirs();
            }
            attachment.setFileName(file.getOriginalFilename());
            attachment.setCheckedStatus(SysAttachmentDo.CHECKEDSTATUS_INIT);
            attachment.setFilePath(path+saveFileName);
            attachment.setFileType(fileType);
            attachment.setGroupType(groupType);
            attachment.setCreateUserId(currentUser().getId());
            Integer dbRes=  sysAttachmentService.insertSysAttachment(attachment);
            AssertUtils.isTrue(dbRes!=null && dbRes>0,"保存失败");
//			String APP_SERVER_URL=paramService.queryValueByCode("APP_SERVER_URL");
//            String APP_SERVER_URL=confService.appUrl();
//            view.put("url",APP_SERVER_URL+"/v1/attachmentIcon.do?id="+attachment.getId().toString());
            //保存
            file.transferTo(targetFile);

            view.put("error",0);

        }catch(BusinessException e)
        {
            view.put("error",1);
            view.put("message",e.getMessage());
        }catch (Exception e) {
            logger.error("上传Excel文件异常", e);
            view.put("error",1);
            view.put("message","上传失败");
        }
        return view;
    }



    @RequestMapping(value="/file",method=RequestMethod.GET)
    public void attachment_methodGet(HttpServletRequest request, HttpServletResponse response, Long id)
    {
        try {
            SysAttachmentDo attachment =sysAttachmentService.findSysAttachmentById(id);
            if(attachment!=null) {
                File file = new File(attachment.getFilePath());
                if (file.exists()) {
                    String fileType = attachment.getFileType();
                    if(fileType!=null)
                    {
                        fileType=fileType.toLowerCase();
                    }
                    if (StringUtils.isNotBlank(fileType) && ("png".equals(fileType)|| "gif".equals(fileType) || "jpg".equals(fileType) || "jpeg".equals(fileType))) {
                        //设置MIME类型
                        response.setHeader("Pragma", "no-cache");
                        response.setHeader("Cache-Control", "no-cache");
                        response.setDateHeader("Expires", 0);
                        response.setContentType("image/" + attachment.getFileType());
                    } else {
                        response.setHeader("Content-disposition", "attachment; filename=" + attachment.getFileName());
                    }
                    responseFile(response, file);
                }
            }
        }catch (Exception e) {
            logger.error("获取附件失败",e);
        }

    }

    @RequestMapping(value="/image",method=RequestMethod.GET)
    public void attachmentIcon_methodGet(HttpServletRequest request,HttpServletResponse response,Long id,Integer w,Integer h,Integer r)
    {
        try {
            File file=null;
            String fileType=null;
            SysAttachmentDo attachment =sysAttachmentService.findSysAttachmentById(id);
            if(attachment!=null && attachment.getFileType()!=null)
            {
                String filePath=attachment.getFilePath();
                fileType=attachment.getFileType().toLowerCase();
                if("pdf".equals(fileType))
                {
                    String classesFile=this.getClass().getClassLoader().getResource("/").getPath();
                    String resPath=new File(classesFile).getParentFile().getParentFile().getPath();
                    filePath=resPath+"/res/images/icon/"+PDF_ICON;
                    fileType="png";
                }else if("doc".equals(fileType) || "docx".equals(fileType)){
                    String classesFile=this.getClass().getClassLoader().getResource("/").getPath();
                    String resPath=new File(classesFile).getParentFile().getParentFile().getPath();
                    filePath=resPath+"/res/images/icon/"+WORD_ICON;
                    fileType="png";
                }else if("xlsx".equals(fileType) || "xls".equals(fileType))
                {
                    String classesFile=this.getClass().getClassLoader().getResource("/").getPath();
                    String resPath=new File(classesFile).getParentFile().getParentFile().getPath();
                    filePath=resPath+"/res/images/icon/"+EXCEL_ICON;
                    fileType="png";
                }
                file=new File(filePath);
            }

            if(file!=null && file.exists()){
                //设置MIME类型
                response.setHeader("Pragma", "no-cache");
                response.setHeader("Cache-Control", "no-cache");
                response.setDateHeader("Expires", 0);
                response.setContentType("image/"+fileType);

                if(w!=null && h!=null)
                {
                    if(r!=null)
                    {
                        BufferedImage image=(BufferedImage) ImageIO.read(file);
                        BufferedImage img= ImageUtils.resize(image,w,h);
                        img=ImageUtils.setClip(img,r);
                        OutputStream out= response.getOutputStream();
                        ImageIO.write(img,"png",out);
                        out.close();
                    }else{
                        BufferedImage image=(BufferedImage) ImageIO.read(file);
                        BufferedImage img= ImageUtils.resize(image,w,h);
                        OutputStream out= response.getOutputStream();
                        ImageIO.write(img,"png",out);
                        out.close();
                    }
                }


                responseFile(response,file);
            }
        }catch (Exception e) {
            logger.error("获取附件失败",e);
        }

    }

    public static void responseFile(HttpServletResponse response, File imgFile) {
        try(InputStream is = new FileInputStream(imgFile);
            OutputStream os = response.getOutputStream();){
            byte [] buffer = new byte[1024]; // 图片文件流缓存池
            while(is.read(buffer) != -1){
                os.write(buffer);
            }
            os.flush();
        } catch (IOException ioe){
            ioe.printStackTrace();
        }
    }
}
