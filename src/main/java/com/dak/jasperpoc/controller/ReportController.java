package com.dak.jasperpoc.controller;

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import javax.transaction.Transactional;

import org.hibernate.mapping.Collection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.dak.jasperpoc.model.Employee;
import com.dak.jasperpoc.repository.EmployeeRepository;
import com.dak.jasperpoc.service.ReportService;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;

@Controller
@RequestMapping("/")
public class ReportController {

	private final EmployeeRepository employeeRepository;
	private final ReportService reportService;
	private final DataSource dataSource;

	@Autowired
	public ReportController(final EmployeeRepository employeeRepository, final ReportService reportService, final DataSource dataSource){
		this.employeeRepository = employeeRepository;
		this.reportService = reportService;
		this.dataSource = dataSource;
	}

	@RequestMapping(method = RequestMethod.GET)
	public String getHome(){
		return "redirect:/employeeReport.pdf";
	}

	@RequestMapping(value = "/employeeReport.pdf", method = RequestMethod.GET, produces = "application/pdf")
	public void getEmployeeReportPdf(final HttpServletResponse response) throws JRException, IOException, ClassNotFoundException, SQLException {
		InputStream jasperStream = this.getClass().getResourceAsStream("/reports/Employees.jrxml");
		JasperReport jasperReport = JasperCompileManager.compileReport(jasperStream);
	//	IOUtils.closeQuietly(jasperStream);
		
		Map<String,Object> params = new HashMap<>();
		//JasperPrint jp = JasperFillManager.fillReport(jasperReport, params, new JRBeanCollectionDataSource(employeeRepository.findAll()));
		
		//JasperPrint jp = JasperFillManager.fillReport(jasperReport, params, new JRBeanCollectionDataSource(new ArrayList<>()));
		JasperPrint jp = JasperFillManager.fillReport(jasperReport, params, dataSource.getConnection());

		reportService.writePdfReport(jp, response, "employeeReport");
		return;
	}

	@RequestMapping(value = "/employeeReport.xlsx", method = RequestMethod.GET, produces = "application/pdf")
	public void getEmployeeReportXlsx(final HttpServletResponse response) throws JRException, IOException, ClassNotFoundException {
		//EmployeeReport report = new EmployeeReport(employeeRepository.findAll());
		//JasperPrint jp = report.getReport();

		JasperPrint jp = null;

		reportService.writeXlsxReport(jp, response, "employeeReport");
		return;
	}

	@PostConstruct
	@Transactional
	public void createTestEmployeeData(){
		final String[] employeeNames = {"David Smith", "Mike Jones", "John Jackson", "Pierre Williams", "Bob Roberts"};

		final List<Employee> employees = new ArrayList<>(employeeNames.length);

		int employeeNumber = 100;
		for(String name : employeeNames){
			employees.add(Employee.builder()
					.empNo(employeeNumber)
					.commission((float)employeeNumber / 75f)
					.salary(employeeNumber * 888)
					.name(name)
					.build());

			employeeNumber++;
		}

		employeeRepository.save(employees);
	}
}
