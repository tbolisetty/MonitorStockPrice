package controller;

import data.Stock;
import database.DBManager;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import static org.junit.Assert.*;

/**
 * Created by tejas on 4/9/2016.
 */
public class CompanyControllerTest {
    DBManager db;
    CompanyController controller;
    @Before
    public void setUp() throws Exception {
        db = new DBManager();
         controller= new CompanyController();
    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void testDeleteCompany() throws Exception {
        assertTrue(true==controller.deleteCompany(1,"AAPL"));

    }

    @Test
    public void testListCompanies() throws Exception {
        ArrayList<Stock> stocks = db.getCompaniesList(1);
        assertEquals(4, controller.listCompanies(1).size());
    }

    @Test
    public void testRefresh() throws Exception {


    }

    @Test
    public void testCompanyHistory() throws Exception {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-mm-dd");
        String dateInString = "2016-04-08";
        Date startDate = sdf.parse(dateInString);
        Date endDate = new Date();

        assertEquals(2,controller.companyHistory(1,"YHOO",startDate,endDate).size());
    }

    @Test
    public void testAddNewCompany() throws Exception {
        assertTrue(false==controller.addNewCompany(1,"YHOO"));
        assertTrue(false==controller.addNewCompany(1,"TWTR"));

    }
}