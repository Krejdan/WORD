package tables;

import java.io.Serializable;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

@Entity
@Table(name="Cars")
public class Car implements Serializable{
	
	@Transient
	private static final long serialVersionUID = 668051766509893654L;

	@Id
	@Column(name="car_id")
	private int carNumber;
	
	@ManyToOne(fetch=FetchType.EAGER)
	@JoinColumn(name="category_id")
	private ExamCategory examCategory;

	public Car() {
		super();
	}

	public Car(int carNumber, ExamCategory examCategory) {
		super();
		this.carNumber = carNumber;
		this.examCategory = examCategory;
	}

	public int getCarNumber() {
		return carNumber;
	}

	public void setCarNumber(int carNumber) {
		this.carNumber = carNumber;
	}

	public ExamCategory getExamCategory() {
		return examCategory;
	}

	public void setExamCategory(ExamCategory examCategory) {
		this.examCategory = examCategory;
	}
	
	
	
};
