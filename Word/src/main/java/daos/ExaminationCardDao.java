package daos;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.persistence.Query;

import org.hibernate.Session;
import org.hibernate.Transaction;

import tables.Car;
import tables.ExamCategory;
import tables.ExaminationCard;
import tables.Term;
import tables.User;
import util.HibernateUtil;

public class ExaminationCardDao implements Dao<ExaminationCard> {
	
	@Override
	public void add(ExaminationCard entity) {
		Transaction transaction = null;
    	Session session = HibernateUtil.getSessionFactory().openSession();
  
    	try {
            transaction = session.beginTransaction();
            session.persist(entity);
            session.saveOrUpdate(entity.getTerm());
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
        } finally {
        	session.close();
        }	
	}

	@Override
	public void delete(ExaminationCard entity) {
		Transaction transaction = null;
    	Session session = HibernateUtil.getSessionFactory().openSession();
  
    	try {
            transaction = session.beginTransaction();
            session.delete(entity);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
        } finally {
        	session.close();
        }
		
	}

	@Override
	public void update(ExaminationCard entity) {
		Transaction transaction = null;
    	Session session = HibernateUtil.getSessionFactory().openSession();
  
    	try {
            transaction = session.beginTransaction();
            session.update(entity);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
        } finally {
        	session.close();
        }
		
	}

	public ExaminationCard get(int id) {
		Transaction transaction = null;
    	Session session = HibernateUtil.getSessionFactory().openSession();
    	ExaminationCard examinationCard = null;
    	
    	try {
            transaction = session.beginTransaction();
            examinationCard = session.get(ExaminationCard.class, id);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
        } finally {
        	session.close();
        }
		return examinationCard;
	}

	@Override
	public List<ExaminationCard> getAll() {
		Transaction transaction = null;
    	Session session = HibernateUtil.getSessionFactory().openSession();
    	List<ExaminationCard> examinationCards = null;
    	
		try {
			transaction = session.beginTransaction();
			examinationCards = session.createQuery("from ExaminationCard", ExaminationCard.class).list();
            transaction.commit();
        } catch (Exception e) {
        	if(transaction != null) {
        		transaction.rollback();
        	}
        	e.printStackTrace();
        } finally {
        	session.close();
        }
		return examinationCards;
	}
	
	public ExaminationCard getByTermAndCar(Term term, int car) {
		Transaction transaction = null;
    	Session session = HibernateUtil.getSessionFactory().openSession();
    	ExaminationCard examinationCard = null;
    	
		try {
			transaction = session.beginTransaction();
			Query query = session.createQuery("from ExaminationCard where term = :term and carNumber = :car", ExaminationCard.class);
			query.setParameter("term", term);
			query.setParameter("car", car);
			examinationCard = (ExaminationCard) query.getSingleResult();
            transaction.commit();
        } catch (Exception e) {
        	if(transaction != null) {
        		transaction.rollback();
        	}
        	e.printStackTrace();
        } finally {
        	session.close();
        }
		return examinationCard;
	}
	
	public ExaminationCard getFirstAvailable() {
		Transaction transaction = null;
    	Session session = HibernateUtil.getSessionFactory().openSession();
    	ExaminationCard examinationCard = null;
    	
		try {
			transaction = session.beginTransaction();
			Query query = session.createQuery("from ExaminationCard where examiner is null", ExaminationCard.class);
			if(!query.getResultList().isEmpty())
				examinationCard = (ExaminationCard) query.getResultList().get(0);
            transaction.commit();
        } catch (Exception e) {
        	if(transaction != null) {
        		transaction.rollback();
        	}
        	e.printStackTrace();
        } finally {
        	session.close();
        }
		return examinationCard;
	}
	
	public int numberOfCarsUsed(Term term) {
		Transaction transaction = null;
		Session session = HibernateUtil.getSessionFactory().openSession();
		int numberOfCarsUsed = 0;
		
		try {
			transaction = session.beginTransaction();
			Query query = session.createQuery("select count(carNumber) from ExaminationCard where term = :term");
			query.setParameter("term", term);
			numberOfCarsUsed = (int)(long)query.getSingleResult();
			transaction.commit();
		} catch (Exception e) {
        	if(transaction != null) {
        		transaction.rollback();
        	}
        	e.printStackTrace();
        } finally {
        	session.close();
        }
		return numberOfCarsUsed;
	}
	
	public List<Car> getCars(Term term, ExamCategory category) {
		Transaction transaction = null;
		Session session = HibernateUtil.getSessionFactory().openSession();
		List<Car> cars = null;
		
		try {
			transaction = session.beginTransaction();
			Query query = session.createQuery("select carNumber from ExaminationCard e where e.term = :term and e.category = :category", Car.class);
			query.setParameter("term", term);
			query.setParameter("category", category);
			cars = castList(Car.class, query.getResultList());
			transaction.commit();
		} catch (Exception e) {
        	if(transaction != null) {
        		transaction.rollback();
        	}
        	e.printStackTrace();
        } finally {
        	session.close();
        }
		return cars;
	}
	
	public ExaminationCard getByTerm(Term term) throws Exception{
		Transaction transaction = null;
    	Session session = HibernateUtil.getSessionFactory().openSession();
    	ExaminationCard examinationCard = null;
    	
		try {
			transaction = session.beginTransaction();
			Query query = session.createQuery("from ExaminationCard where term = :term", ExaminationCard.class);
			query.setParameter("term", term);
			examinationCard = (ExaminationCard) query.getSingleResult();
            transaction.commit();
        } catch (Exception e) {
        	if(transaction != null) {
        		transaction.rollback();
        	}
        	throw e;
        } finally {
        	session.close();
        }
		return examinationCard;
	}
	
	public ExaminationCard getByTermAndCategory(Term term, ExamCategory category) throws Exception{
		Transaction transaction = null;
    	Session session = HibernateUtil.getSessionFactory().openSession();
    	ExaminationCard examinationCard = null;
    	
		try {
			transaction = session.beginTransaction();
			Query query = session.createQuery("from ExaminationCard where term = :term and category = :category", ExaminationCard.class);
			query.setParameter("term", term);
			query.setParameter("category", category);
			examinationCard = (ExaminationCard) query.getSingleResult();
            transaction.commit();
        } catch (Exception e) {
        	if(transaction != null) {
        		transaction.rollback();
        	}
        	throw e;
        } finally {
        	session.close();
        }
		return examinationCard;
	}
	
	public List<ExaminationCard> getByUserAndCategory(User user, ExamCategory category) throws Exception{
		Transaction transaction = null;
    	Session session = HibernateUtil.getSessionFactory().openSession();
    	List<ExaminationCard> examinationCards = null;
    	
		try {
			transaction = session.beginTransaction();
			Query query = session.createQuery("from ExaminationCard where examined = :user and category = :category and result is not null", ExaminationCard.class);
			query.setParameter("user", user);
			query.setParameter("category", category);
			examinationCards = castList(ExaminationCard.class, query.getResultList());
            transaction.commit();
        } catch (Exception e) {
        	if(transaction != null) {
        		transaction.rollback();
        	}
        	throw e;
        } finally {
        	session.close();
        }
		return examinationCards;
	}
	
	public List<ExaminationCard> getByUser(User user) throws Exception{
		Transaction transaction = null;
    	Session session = HibernateUtil.getSessionFactory().openSession();
    	List<ExaminationCard> examinationCards = null;
    	
		try {
			transaction = session.beginTransaction();
			Query query = session.createQuery("from ExaminationCard where examined = :user and result is not null", ExaminationCard.class);
			query.setParameter("user", user);
			examinationCards = castList(ExaminationCard.class, query.getResultList());
            transaction.commit();
        } catch (Exception e) {
        	if(transaction != null) {
        		transaction.rollback();
        	}
        	throw e;
        } finally {
        	session.close();
        }
		return examinationCards;
	}
	
	public List<ExaminationCard> getByUserAndCategoryNull(User user, ExamCategory category) throws Exception{
		Transaction transaction = null;
    	Session session = HibernateUtil.getSessionFactory().openSession();
    	List<ExaminationCard> examinationCards = null;
    	
		try {
			transaction = session.beginTransaction();
			Query query = session.createQuery("from ExaminationCard where examined = :user and category = :category and result is null", ExaminationCard.class);
			query.setParameter("user", user);
			query.setParameter("category", category);
			examinationCards = castList(ExaminationCard.class, query.getResultList());
            transaction.commit();
        } catch (Exception e) {
        	if(transaction != null) {
        		transaction.rollback();
        	}
        	throw e;
        } finally {
        	session.close();
        }
		return examinationCards;
	}
	
	
	public static <T> List<T> castList(Class<? extends T> clazz, Collection<?> c) {
	    List<T> r = new ArrayList<T>(c.size());
	    for(Object o: c)
	      r.add(clazz.cast(o));
	    return r;
	}
}
