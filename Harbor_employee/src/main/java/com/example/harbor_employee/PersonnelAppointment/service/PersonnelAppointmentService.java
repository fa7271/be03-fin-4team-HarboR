package com.example.harbor_employee.PersonnelAppointment.service;

import com.example.harbor_employee.Employee.domain.Employee;
import com.example.harbor_employee.Employee.dto.request.EmployeeSearchDto;
import com.example.harbor_employee.Employee.dto.response.ExcelDataDto;
import com.example.harbor_employee.Employee.repository.EmployeeRepository;
import com.example.harbor_employee.PersonnelAppointment.domain.PersonnelAppointment;
import com.example.harbor_employee.PersonnelAppointment.dto.response.PaResDto;
import com.example.harbor_employee.PersonnelAppointment.repository.PersonnelAppointmentRepository;
import com.example.harbor_employee.global.util.EmployeeSpecification;
import com.example.harbor_employee.global.util.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.formula.functions.T;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@Slf4j
public class PersonnelAppointmentService {

    private final PersonnelAppointmentRepository parepository;
    private final EmployeeRepository employeeRepository;
    private final RedisUtil redisUtil;
    private static final int EMPLOYEE_ID_COLUMN_INDEX = 0;
    private static final int BEFORE_DEPARTMENT_CODE_COLUMN_INDEX = 1;
    private static final int AFTER_DEPARTMENT_CODE_COLUMN_INDEX = 2;
    private static final int POSITION_CODE_COLUMN_INDEX = 3;
    private static final int ISSUE_DATE_COLUMN_INDEX = 4;
    private static final int UPDATE_DUTY_CODE_COLUMN_INDEX = 5;


    public PersonnelAppointmentService(PersonnelAppointmentRepository parepository, EmployeeRepository employeeRepository, RedisUtil redisUtil) {
        this.parepository = parepository;
        this.employeeRepository = employeeRepository;
        this.redisUtil = redisUtil;
    }

    /**
     * @param file 파일
     * @throws IOException 파일 없는경우
     * @return List<ExcelDataDto>
     * @description ilfe 을 불러운다.
     */
    public List<ExcelDataDto> create(MultipartFile file) throws IOException {

        String extension = FilenameUtils.getExtension(file.getOriginalFilename());


        if (!extension.equals("xlsx") && !extension.equals("xls")) {
            throw new IllegalArgumentException("엑셀 파일만 올려주세요.");
        }

        try (InputStream inputStream = file.getInputStream()) {
            Workbook workbook = null;

            if (extension.equals("xlsx")) {
                workbook = new XSSFWorkbook(inputStream);
            } else if (extension.equals("xls")) {
                workbook = new HSSFWorkbook(inputStream);
            }

            List<ExcelDataDto> dataList = new ArrayList<>();
            Sheet worksheet = workbook.getSheetAt(0);

            for (int i = 1; i < worksheet.getPhysicalNumberOfRows(); i++) {
                Row row = worksheet.getRow(i);
                if (row != null) {
                    ExcelDataDto excelDataDto = new ExcelDataDto();
                    Cell cell1 = row.getCell(EMPLOYEE_ID_COLUMN_INDEX);
                    if (cell1 != null)
                        excelDataDto.setEmployeeId(cell1.getStringCellValue());
                    Employee employee = employeeRepository.findByEmployeeId(excelDataDto.getEmployeeId()).orElseThrow(() -> new IllegalArgumentException(" 없는 회원 입니다."));

                    Cell cell2 = row.getCell(BEFORE_DEPARTMENT_CODE_COLUMN_INDEX);
                    if (cell2 != null)
                        excelDataDto.setBeforeDepartmentCode(cell2.getStringCellValue());

                    Cell cell3 = row.getCell(AFTER_DEPARTMENT_CODE_COLUMN_INDEX);
                    if (cell3 != null)
                        excelDataDto.setAfterDepartmentCode(cell3.getStringCellValue());

                    Cell cell4 = row.getCell(POSITION_CODE_COLUMN_INDEX);
                    if (cell4 != null)
                        excelDataDto.setPositionCode(cell4.getStringCellValue());

                    Cell cell5 = row.getCell(ISSUE_DATE_COLUMN_INDEX);
                    if (cell5 != null) {
                        if (cell5.getCellType() == CellType.NUMERIC) {
                            Date date = cell5.getDateCellValue();
                            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                            excelDataDto.setIssueDate(dateFormat.format(date));
                        } else if (cell5.getCellType() == CellType.STRING) {
                            excelDataDto.setIssueDate(cell5.getStringCellValue());
                        }
                    }
                    Cell cell6 = row.getCell(UPDATE_DUTY_CODE_COLUMN_INDEX);
                    if (cell6 != null)
                        excelDataDto.setUpdateDutyCode(cell6.getStringCellValue());

                    PersonnelAppointment personnelAppointment = PersonnelAppointment.CreatePA(
                            employee,
                            excelDataDto.getUpdateDutyCode(),
                            excelDataDto.getBeforeDepartmentCode(),
                            excelDataDto.getAfterDepartmentCode(),
                            excelDataDto.getPositionCode(),
                            excelDataDto.getIssueDate()
                            );
                    parepository.save(personnelAppointment);
                    log.info("personnelAppointment {}", "personnelAppointment");
////                    key : 날짜 , value : PA_id
                    if (personnelAppointment.getIssueDate() != null) {
                        String data = redisUtil.getData(excelDataDto.getIssueDate());
//                        이미 있는경우 꺼내와서 + 해서 넣기
                        if (data == null || !data.contains(personnelAppointment.getAppointmentId() + "_")) {
                            data = (data == null) ? personnelAppointment.getAppointmentId() + "_" : data + personnelAppointment.getAppointmentId() + "_";
                            redisUtil.setDataExpire(personnelAppointment.getIssueDate(), data);
                        }
                    }
                    dataList.add(excelDataDto);
                }
            }
            return dataList;
        } catch (IOException e) {
            // 파일 처리 중 예외 발생 시 예외를 던짐
            throw new IOException("파일을 처리하는 도중 오류가 발생");
        }
    }

    public Page<PaResDto> findAll(EmployeeSearchDto employeeSearchDto, Pageable pageable) {
        Specification<T> specification =
                EmployeeSpecification.likeEmployeeId_class(employeeSearchDto.getEmployeeId());

        // Sort 객체를 생성하여 issueDate를 기준으로 오름차순으로 정렬
        Sort sort = Sort.by("issueDate").ascending();

        // Sort 객체를 PageRequest.of 메서드에 전달하여 정렬된 페이지 받기
        Pageable sortedPageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);


        Page<PersonnelAppointment> personnelAppointmentList = parepository.findAllWithIssueDateAfterToday(String.valueOf(LocalDate.now().plusDays(1)), sortedPageable);

        return personnelAppointmentList.map(PaResDto::toPaDto);
    }

    public String delete(Long appointmentId) {
        log.info("personnelAppointment : ");
        PersonnelAppointment personnelAppointment = parepository.findById(appointmentId).orElseThrow(() -> new IllegalArgumentException(" 없는 인사발령 번호 입니다"));
        log.info("personnelAppointment : {} ", personnelAppointment);
        parepository.delete(personnelAppointment);
        return "삭제 완료";
    }

    public PaResDto detail(Long appointmentId) {
        PersonnelAppointment personnelAppointment = parepository.findById(appointmentId).orElseThrow(() -> new IllegalArgumentException("없는 인사 발령정보입니다."));
        return PaResDto.toPaDto(personnelAppointment);
    }
//    **@PostAuthorize("isAuthenticated() and (( returnObject.name == principal.name ) or hasRole('ROLE_ADMIN'))")**
//@RequestMapping( value = "/{seq}", method = RequestMethod.GET )
//public User getuser( @PathVariable("seq") long seq ){
//    return userService.findOne(seq);
//}
}
