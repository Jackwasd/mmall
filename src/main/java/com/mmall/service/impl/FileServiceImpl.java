package com.mmall.service.impl;

import com.google.common.collect.Lists;
import com.mmall.service.IFileService;
import com.mmall.util.FTPUtil;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Service("iFileService")
@Slf4j
public class FileServiceImpl implements IFileService{

    public String upload(MultipartFile file, String path){
        String fileName = file.getOriginalFilename();
        //获取扩展名
        String fileExtension = fileName.substring(fileName.lastIndexOf(".") + 1);
        //防止上传了同名文件导致文件的覆盖
        String uploadFileName = UUID.randomUUID().toString() + "." + fileExtension;
        log.info("开始上传文件，上传文件的文件名:{}, 上传的路径:{},新文件名:{}",fileName,path,uploadFileName);

        //判断上传路径上面的文件夹是否是存在的，不存在的话就需要创建文件夹
        File fileDir = new File(path);
        if(!fileDir.exists()){
            fileDir.setWritable(true);
            fileDir.mkdirs();
        }
        File targetFile = new File(path, uploadFileName);
        try {
            //此处的时候说明文件已经成功的上传到了我们的upload 文件夹下面
            file.transferTo(targetFile);
            //将targetFile上传到我们的FTP服务器下面
            List list = Lists.newArrayList(targetFile);
            FTPUtil.uploadFile(list);
            //将upload文件夹下的文件删除
            targetFile.delete();
        } catch (IOException e) {
            log.error("文件上传异常", e);
            return null;
        }
        return targetFile.getName();
    }

}
