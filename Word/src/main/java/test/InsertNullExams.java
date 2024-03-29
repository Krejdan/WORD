package test;

import daos.CarDao;
import daos.DayDao;
import daos.ExamCategoryDao;
import daos.ExaminationCardDao;
import daos.HourDao;
import daos.MonthDao;
import daos.PositionDao;
import daos.ResourceDao;
import daos.TermDAO;
import daos.TheoreticalExamDao;
import daos.UserDao;
import daos.YearDao;
import tables.ExaminationCard;
import tables.Term;
import tables.TheoreticalExam;
import util.HibernateUtil;

public class InsertNullExams {

	PositionDao positionDao = new PositionDao();
	ResourceDao resourceDao = new ResourceDao();
	UserDao userDao = new UserDao();
	TermDAO terminDao = new TermDAO();
	HourDao hourDao = new HourDao();
	YearDao yearDao = new YearDao();
	MonthDao monthDao = new MonthDao();
	DayDao dayDao = new DayDao();
	CarDao carDao = new CarDao();
	ExamCategoryDao categoryDao = new ExamCategoryDao();
	ExaminationCardDao examCardDao = new ExaminationCardDao();
	TheoreticalExamDao examDao = new TheoreticalExamDao();
	
	public void insertTheoreticalExams()
	{
		Term term = terminDao.get(1);
		TheoreticalExam exam = new TheoreticalExam(categoryDao.get("B"), term, resourceDao.get(1), userDao.get(2));
		exam.addUser(userDao.get(1));
		exam.addUser(userDao.get(3));
		examDao.add(exam);
		exam = new TheoreticalExam(categoryDao.get("A"), term, resourceDao.get(1), userDao.get(2));
		exam.addUser(userDao.get(1));
		exam.addUser(userDao.get(3));
		examDao.add(exam);
		exam = new TheoreticalExam(categoryDao.get("B"), term, resourceDao.get(1), userDao.get(2));
		exam.addUser(userDao.get(1));
		exam.addUser(userDao.get(3));
		examDao.add(exam);
		exam = new TheoreticalExam(categoryDao.get("D"), term, resourceDao.get(1), userDao.get(2));
		exam.addUser(userDao.get(1));
		exam.addUser(userDao.get(3));
		examDao.add(exam);
	}
	
	public void insertExaminationCards() throws Exception
	{
		Term term = terminDao.get(1);
		ExaminationCard examCard = new ExaminationCard(categoryDao.get("B"), carDao.get(1), null, null, term, resourceDao.get(1), null, userDao.get(1));
		examCardDao.add(examCard);
		examCard = new ExaminationCard(categoryDao.get("A"), carDao.get(1), null, null, term, resourceDao.get(1), null, userDao.get(1));
		examCardDao.add(examCard);
		examCard = new ExaminationCard(categoryDao.get("C"), carDao.get(1), null, null, term, resourceDao.get(1), null, userDao.get(1));
		examCardDao.add(examCard);
		examCard = new ExaminationCard(categoryDao.get("A1"), carDao.get(1), null, null, term, resourceDao.get(1), null, userDao.get(1));
		examCardDao.add(examCard);
	}
	
	
	public static void main(String[] args) {
		InsertNullExams ine = new InsertNullExams();
		try {
			ine.insertExaminationCards();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		ine.insertTheoreticalExams();
		HibernateUtil.shutdown();
		
	}
	
}
