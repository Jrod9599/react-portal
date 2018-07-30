package com.syntel.reactportal.controller;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;


@RequestMapping("/file")
@RestController
public class ExcelController {
    @Autowired
    JdbcTemplate jdbcTemplate;

    @CrossOrigin
    @PostMapping("/read")
    public void readExcelFile(@RequestParam("files") MultipartFile[] excelFiles) throws IOException {
        for (MultipartFile file : excelFiles) {
            System.out.println(file.getContentType());
            InputStream inputStream = file.getInputStream();

            Workbook workbook = typedWorkbook(file, inputStream);
            Sheet sheet = workbook.getSheetAt(0);

            String insertSQL = "INSERT INTO PORTAL.PORTAL (DEPT, CLASS, CATEGORY, SUBCATEGORY, `STORE #`) VALUES (?, ?, ?, ?, ?)";
            for (Row row : sheet) {
                if (row.getRowNum() > 2) {
                    Object[] arguments = {row.getCell(0).getRichStringCellValue().getString(), row.getCell(1).getRichStringCellValue().getString(), row.getCell(2).getRichStringCellValue().getString(), row.getCell(3).getRichStringCellValue().getString(), row.getCell(4).getRichStringCellValue().getString()};
                    jdbcTemplate.update(insertSQL, arguments);
                }
            }
        }
    }

    @CrossOrigin
    @RequestMapping(method = RequestMethod.POST, value = "/return")
    public HttpEntity<byte[]> returnExcelFile(@RequestParam("files") MultipartFile[] excelFiles, HttpServletResponse response) throws IllegalStateException, IOException {
        byte[] excelContent = excelFiles[0].getBytes();
        Workbook wb = typedWorkbook(excelFiles[0], excelFiles[0].getInputStream());
//        FileOutputStream out = new FileOutputStream();
//        wb.write(out);
        HttpHeaders header = new HttpHeaders();
        header.setContentType(new MediaType("application", "vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
        header.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=my_file.xls");
        header.setContentLength(excelContent.length);
        response.setContentType("application/vnd.ms-excel");
        return new HttpEntity<byte[]>(excelContent, header);
    }

    public Workbook typedWorkbook(MultipartFile file, InputStream inputStream) throws IOException {
        if (file.getContentType().equalsIgnoreCase("application/vnd.ms-excel")) {
            return new HSSFWorkbook(inputStream);
        } else if (file.getContentType().equalsIgnoreCase("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")) {
            return new XSSFWorkbook(inputStream);
        } else {
            return null;
        }
    }

}
