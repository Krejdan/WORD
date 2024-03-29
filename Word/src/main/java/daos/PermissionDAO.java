package daos;


import java.util.List;

import org.hibernate.Session;
import org.hibernate.Transaction;
import util.HibernateUtil;
import tables.Permission;

public class PermissionDAO {
	public void savePermission(Permission permission) {

        Transaction transaction = null;

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {

            // start a transaction

            transaction = session.beginTransaction();

            // save the object
            
            session.save(permission);

            // commit transaction

            transaction.commit();
            System.out.println("Dodawanie udane");

        } catch (Exception e) {

            if (transaction != null) {

                transaction.rollback();

            }

            e.printStackTrace();

        }

    }
	public List < Permission > getPermissions() {

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {

            return session.createQuery("from Permissions", Permission.class).list();

        }

    }
}
