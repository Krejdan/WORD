package server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.persistence.NoResultException;

import daos.*;
import javafx.scene.control.ListView;
import tables.*;
import util.Connection;
import util.HibernateUtil;

public class Server {

	private static final int PORT = 25565;
	private static List<User> users = Collections.synchronizedList(new ArrayList<>());
	private static final int NUMBER_OF_EXAMINEE = 1;
	private static void initUsers() throws NoResultException {
		TheoreticalExamDao tedao = new TheoreticalExamDao();
		TheoreticalExam theoreticalExam = tedao.getOpen();
		users.addAll(theoreticalExam.getUsers());
	}
	
	private static void initDatabase() {
		CarDao carDao = new CarDao();
		try {
			if(carDao.get(0) != null)
				return;
			else {
				System.out.println("Inicjalizacja bazy danych");
				util.InitializeDatabase.initializeCategories();
				util.InitializeDatabase.initializeCars();
				util.InitializeDatabase.initializePositions();
				util.InitializeDatabase.initializeResources();
				util.InitializeDatabase.initializeQuestions();
				util.InitializeDatabase.initializeTaskTypes();
				util.InitializeDatabase.initializeTasks();
				util.InitializeDatabase.initializeTerms();
				util.InitializeDatabase.initializeUsers();
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
			
	}

	public static void main(String[] args) throws Exception {
		ServerSocket listener = new ServerSocket(PORT);
		HibernateUtil.getSessionFactory();
		initDatabase();
		try {
			initUsers();
		} catch (NoResultException e) {
			System.out.println("Brak egzaminow otwartych po ponownym starcie serwera");
		}
		System.out.println("Serwer otwarty");
		try {
			while (true) {
				System.out.println("Oczekiwanie na klienta");
				new Handler(listener.accept()).start();
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			listener.close();
		}
	}

	private static class Handler extends Thread {
		private Socket socket;
		private ObjectInputStream input;
		private OutputStream os;
		private ObjectOutputStream output;
		private InputStream is;

		private ExamResultDao examResultDao = new ExamResultDao();
		private ResourceDao resourceDao = new ResourceDao();
		private UserDao userDao = new UserDao();
		private TheoreticalExamDao tedao = new TheoreticalExamDao();
		private ExaminationCardDao examDao = new ExaminationCardDao();
		private QuestionDao questionDao = new QuestionDao();
		private QuestionResultDao questionResultDao = new QuestionResultDao();
		private ExamTaskDao examTaskDao = new ExamTaskDao();
		private ExamTaskResultDao examTaskResultdao = new ExamTaskResultDao();
		private List<TheoreticalExam> theoreticalExams = null;
		private Car[] freeCarArray = new Car[21];

		public Handler(Socket socket) throws IOException {
			this.socket = socket;
		}

		public void getTheoreticalExams(Request request) throws IOException {
			MonthDao monthDao = new MonthDao();
			DayDao dayDao = new DayDao();
			List<TheoreticalExam> theoreticalExams = new ArrayList<>();
			Month month = monthDao.get(LocalDate.now().getMonthValue());
			Day day = dayDao.get(LocalDate.now().getDayOfMonth());
			theoreticalExams = tedao.getNotOpen(); 
			theoreticalExams.removeIf(theoreticalExam -> theoreticalExam.getIsOpen() != 0);
			output.writeObject(theoreticalExams);
		}

		public void startTheoreticalExam(Request request) throws IOException {
			TheoreticalExam theoreticalExam = new TheoreticalExam();
			User user = request.getUser();
			Request send = new Request();
			try {
				theoreticalExam = tedao.getOpen();
				if (users.contains(user)) {
					send.setResult(true);
					send.setQuestions(questionDao.getRandomQuestions(10));
					send.setTheoreticalExam(theoreticalExam);
					users.remove(users.indexOf(request.getUser()));
				} else {
					send.setResult(false);
					send.setMessage(
							"Nie znajdujesz si� na li�cie otwartego egzaminu lub egzamin z tego konta zosta� ju� uruchomiony");
				}
				if (users.isEmpty()) {
					theoreticalExam.setIsOpen((short) 2);
					tedao.update(theoreticalExam);
				}
			} catch (NoResultException e) {
				send.setResult(false);
				send.setMessage("�aden egzamin nie jest aktualnie otwarty");
			} finally {
				output.writeObject(send);
			}
		}

		public void getExaminationCard(Request request) throws IOException {
			User user = request.getUser();
			Request send = new Request();
			ExaminationCard examCard = examDao.getFirstAvailable();
			if (examCard != null) {
				examCard.setExaminer(user);
				examDao.update(examCard);
				send.setResult(true);
				send.setExaminationCard(examCard);
			} else {
				send.setResult(false);
				send.setMessage("Brak dostepnych kart egzaminacyjnych");
			}
			output.writeObject(send);
		}

		public void endTheoreticalExam(Request request) throws IOException {
			Request send = new Request();
			int i = 0;
			int points = 0;
			List<QuestionResult> questionResults = new ArrayList<>();
			ExamResult examResult = new ExamResult(" ", request.getTheoreticalExam(),
					resourceDao.getByName("ExamResult"), userDao.get(request.getUser().getEmail()));
			examResultDao.add(examResult);
			List<String> answers = request.getAnswers();
			List<Question> questions = request.getQuestions();
			for (Question question : questions) {
				if (answers.get(i).equals(question.getCorrectAnswer())) {
					questionResults.add(new QuestionResult(question, examResult, 1, answers.get(i)));
					points++;
				} else {
					questionResults.add(new QuestionResult(question, examResult, 0, answers.get(i)));
				}
				i++;
			}
			examResult.setQuestions(questionResults);
			if (points > (questions.size() / 2)) {
				examResult.setWynik("POZYTYWNY");
				List<TheoreticalExam> exams = tedao.getNotOpen();
				System.out.println(exams);
				for(TheoreticalExam exam : exams) {
					System.out.println(exam);
					System.out.println(request.getUser());
					if(exam.getUsers().contains(request.getUser())) {
						exam.removeUser(request.getUser());
						tedao.update(exam);
					}
				}
			}	
			else
				examResult.setWynik("NEGATYWNY");
			examResultDao.update(examResult);
			send.setPoints(points);
			output.writeObject(send);
		}

		public void sendExaminationCard(Request request) {
			ExaminationCard examinationCard = request.getExaminationCard();
			examinationCard.setResult(request.getMessage());
			examinationCard.setComments(request.getMail());
			examinationCard.setExamTaskResults(request.getExamTaskResults());	
			examDao.update(examinationCard);
			if(request.getMessage().equals("POZYTYWNY")) {
				try {
					for(ExaminationCard exam : examDao.getByUserAndCategoryNull(request.getExaminationCard().getExamined(), request.getExaminationCard().getCategory()))
						examDao.delete(exam);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

		public void login(Request request) throws IOException {
			String mail = request.getMail();
			String password = request.getPassword();
			Request send = new Request();
			try {
				User user = userDao.get(mail);
				if (user.getPassword().equals(password)) {
					send.setResult(true);
					send.setUser(user);
				} else {
					send.setResult(false);
					send.setMessage("B��dne has�o");
				}
			} catch (NoResultException e) {
				send.setResult(false);
				send.setMessage("U�ytkownik o podanym mailu nie istnieje");
			} finally {
				output.writeObject(send);
			}
		}

		public void openTheoreticalExam(Request request) throws IOException {
			Boolean exists = false;
			List<TheoreticalExam> theoreticalExams = new ArrayList<>();
			theoreticalExams = new ArrayList<>();
			theoreticalExams = tedao.getAll();
			for (TheoreticalExam theoreticalExam : theoreticalExams) {
				if (theoreticalExam.getIsOpen() == 1) {
					Connection.request = new Request();
					Connection.request.setMessage(
							"Istnieje ju� otwarty egzamin (Egzamin o id: " + theoreticalExam.getExamId() + ")");
					Connection.request.setResult(false);
					output.writeObject(Connection.request);
					exists = true;
				}
				break;
			}
			if (!exists) {
				TheoreticalExam theoreticalExam = request.getTheoreticalExam();
				users.addAll(theoreticalExam.getUsers());
				theoreticalExam.setIsOpen((short) 1);
				theoreticalExam.setExaminer(request.getUser());
				tedao.update(theoreticalExam);
				Connection.request = new Request();
				Connection.request.setResult(true);
				Connection.request.setMessage("Otwarcie powiod�o si�");
				output.writeObject(Connection.request);
			}
		}

		public boolean passedPractical(User user, ExamCategory category) {
			List<ExaminationCard> exams = null;
			try {
				exams = examDao.getByUserAndCategory(user, category);
			} catch (Exception e) {
				e.printStackTrace();
			}
			for (ExaminationCard exam : exams) {
				if(exam.getResult().equals("POZYTYWNY"))
					return true;
			}
			return false;
		}
		
		public void getFreeTerms(Request request) {
			DayDao dayDao = new DayDao();
			YearDao yearDao = new YearDao();
			MonthDao monthDao = new MonthDao();
			HourDao hourDao = new HourDao();
			TermDAO termDao = new TermDAO();
			CarDao carDao = new CarDao();
			ExaminationCardDao examinationCardDao = new ExaminationCardDao();
			List<Term> termsList = new ArrayList<>();
			
			Request send = new Request();
			Term term = new Term();
			
			
			LocalDate localDate = LocalDate.now();
			if (localDate.compareTo(request.getDate()) >= 0) {
				send.setResult(false);
				send.setMessage("Prosimy o dokonanie rezerwacji terminu z jednodniowym wyprzedzeniem");
				try {
					output.writeObject(send);
					return;
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			else {
				send.setResult(true);
				Year year = yearDao.get(request.getDate().getYear());
				Month month = monthDao.get(request.getDate().getMonthValue());
				Day day = dayDao.get(request.getDate().getDayOfMonth());
				
				TheoreticalExamDao theoreticalExamDao = new TheoreticalExamDao();
				
				if (canOrderPractical(request.getUser(), request.getCategory())) {
					send.setCanOrderPractical(true);
					if(passedPractical(request.getUser(), request.getCategory())) {
						send.setResult(false);
						send.setMessage("Wszystkie egzaminy danej kategorii zostaly zdane");
						try {
							output.writeObject(send);
							return;
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
					for (int hourID = 1; hourID <= 20; hourID++) {
						try {
							term = termDao.get(year, month, day, hourDao.get(hourID));
						} catch (Exception e) {
							term = new Term(year, month, day, hourDao.get(hourID));
							termDao.add(term);
						}
	
						List<Car> busyCars = new ArrayList<>();
						List<Car> allCars = carDao.getByCategory(request.getCategory());
						try {
							if (examinationCardDao.getByTermAndCategory(term, request.getCategory()) != null)
								busyCars = examinationCardDao.getCars(term, request.getCategory());
						} catch (Exception e) {
							e.printStackTrace();
						}
						if (allCars.size() > busyCars.size()) {			
							for (Car car : allCars) {
								if (busyCars == null || !busyCars.contains(car)) {
									freeCarArray[hourID] = car;
									break;
								}
							}
							termsList.add(term);
						}
					}
				} else {
					send.setCanOrderPractical(false);
					for (int hourID = 1; hourID <= 20; hourID++) {
						try {
							term = termDao.get(year, month, day, hourDao.get(hourID));
						} catch (Exception e) {
							term = new Term(year, month, day, hourDao.get(hourID));
							termDao.add(term);
						}
						try {
							TheoreticalExam exam = theoreticalExamDao.getByTermAndCategory(term, request.getCategory());
							if (exam == null
									|| (exam.getUsers().size() < NUMBER_OF_EXAMINEE && !exam.getUsers().contains(request.getUser()))) {
								termsList.add(term);
							}
						} catch (Exception e) {
							termsList.add(term);
						}
					}
	
				}
				send.setTermsList(termsList);
				try {
					output.writeObject(send);
					return;
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		private boolean canOrderPractical(User user, ExamCategory category) {
			theoreticalExams = user.getTheoreticalExams();
			
			ExamResultDao examResultDao = new ExamResultDao();

			for (TheoreticalExam exam : theoreticalExams) {
				if(exam.getCategory().getCategoryId() == category.getCategoryId()) {
					List<ExamResult> examResults = examResultDao.get(exam);
					for (ExamResult result : examResults) {
						if (result.getEgzaminowany().equals(user) && result.getWynik().equals("POZYTYWNY"))
							return true;
					}
				}
			}
			return false;
		}

		public void getTasks(Request request) throws IOException {
			output.writeObject(examTaskDao.get(request.getExaminationCard().getCategory()));
		}

		private void orderPractical(Request request) throws IOException {
			Request send = new Request();
			ExaminationCard examinationCard = new ExaminationCard(request.getCategory(),
					freeCarArray[request.getTerm().getHour().getHourId()], null, null, request.getTerm(),
					resourceDao.getByName("ExaminationCard"), null, request.getUser());
			ExaminationCardDao examinationCardDao = new ExaminationCardDao();
			examinationCardDao.add(examinationCard);
			send.setMessage("Dodawanie zako�czone sukcesem");
			output.writeObject(send);
		}

		private void orderTheoretical(Request request) throws IOException {
			TheoreticalExamDao theoreticalExamDao = new TheoreticalExamDao();
			Request send = new Request();
			try {
				TheoreticalExam exam = theoreticalExamDao.getByTermAndCategory(request.getTerm(),
						request.getCategory());
				if (!exam.getUsers().contains(request.getUser())) {
					exam.addUser(request.getUser());
					send.setMessage("Dodawanie zako�czone sukcesem");
					theoreticalExamDao.update(exam);
				} else
					send.setMessage("Na podanym egzaminie jest ju� zapisany u�ytkownik z tym mailem");
			} catch (Exception e) {
				TheoreticalExam exam = new TheoreticalExam(request.getCategory(), request.getTerm(),
						resourceDao.getByName("TheoreticalExam"), null);
				exam.addUser(request.getUser());
				theoreticalExamDao.add(exam);
				send.setMessage("Dodawanie zako�czone sukcesem");
			}
			output.writeObject(send);

		}

		public void getCategories(Request request) throws IOException {
			ExamCategoryDao examCategoryDao = new ExamCategoryDao();
			output.writeObject(examCategoryDao.getAll());
		}
		
		public void getTheoreticalExamsForUser(Request request) throws IOException {
			List<ExamResult> examResults = new ArrayList<>();
			examResults = examResultDao.get(request.getUser()); 
			output.writeObject(examResults);
		}
		
		public void getQuestions(Request request) throws IOException {
			List<QuestionResult> questionResults = new ArrayList<>();
			questionResults = questionResultDao.get(request.getExamResult()); 
			output.writeObject(questionResults);
		}
		
		public void getExaminationCards(Request request) throws IOException {
			List<ExaminationCard> examinationCards = new ArrayList<>();
			try {
				examinationCards = examDao.getByUser(request.getUser());
			} catch (Exception e) {
				e.printStackTrace();
			}
			output.writeObject(examinationCards);
		}
		
		public void getTaskResults(Request request) {
			List<ExamTaskResult> examTaskResults = examTaskResultdao.getByExam(request.getExaminationCard());
			try {
				output.writeObject(examTaskResults);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		public void getPositions(Request request) {
			PositionDao positionDao = new PositionDao();
			List<Position> positions = positionDao.getAll();
			try {
				output.writeObject(positions);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		public void addUser(Request request) throws IOException {
			Request send = new Request();
			UserDao userDao = new UserDao();
			try {
				userDao.get(request.getUser().getEmail());
				send.setResult(false);
				send.setMessage("U�ytkownik o podanym mailu jest ju� zarejestrowany");
			} catch (NoResultException e) {
				send.setResult(true);
				userDao.add(request.getUser());
			} finally {
				output.writeObject(send);
			}
		}
		
		public void getUsers(Request request) throws IOException {
			List<User> users;
			UserDao userDao = new UserDao();
			users = userDao.getAll();
			output.writeObject(users);
		}
		@Override
		public void run() {
			try {

				System.out.println("Po��czono z klientem");
				os = socket.getOutputStream();
				output = new ObjectOutputStream(os);
				is = socket.getInputStream();
				input = new ObjectInputStream(is);
				while (socket.isConnected()) {
					Request request = (Request) input.readObject();
					switch (request.getType()) {
					case GET_THEORETICAL_EXAMS:
						getTheoreticalExams(request);
						break;
					case START_THEORETICAL_EXAM:
						startTheoreticalExam(request);
						break;
					case GET_EXAMINATION_CARD:
						getExaminationCard(request);
						break;
					case END_THEORETICAL_EXAM:
						endTheoreticalExam(request);
						break;
					case SEND_EXAMINATION_CARD:
						sendExaminationCard(request);
						break;
					case GET_FREE_TERMS:
						getFreeTerms(request);
						break;
					case LOGIN:
						login(request);
						break;
					case OPEN_THEORETICAL_EXAM:
						openTheoreticalExam(request);
						break;
					case GET_TASKS:
						getTasks(request);
						break;
					case ORDER_PRACTICAL:
						orderPractical(request);
						break;
					case ORDER_THEORETICAL:
						orderTheoretical(request);
						break;
					case GET_CATEGORIES:
						getCategories(request);
						break;
					case GET_THEORETICAL_EXAMS_FOR_USER:
						getTheoreticalExamsForUser(request);
						break;
					case GET_QUESTIONS:
						getQuestions(request);
						break;
					case GET_EXAMINATION_CARDS:
						getExaminationCards(request);
						break;
					case GET_TASK_RESULTS:
						getTaskResults(request);
						break;
					case GET_POSITIONS:
						getPositions(request);
						break;
					case ADD_USER:
						addUser(request);
						break;
					case GET_USERS:
						getUsers(request);
						break;
					}
				}

			} catch (EOFException e1) {
				System.out.println("Klient zako�czy� po��czenie!");
			} catch (Exception e) {
				e.printStackTrace();
			}

		}
	}
}
