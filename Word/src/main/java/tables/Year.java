package tables;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

@Entity
@Table(name="year")
public class Year implements Serializable{

	@Transient
	private static final long serialVersionUID = -6430918898311168955L;
	
	@Id
	@Column(name="year_number")
	private int yearNumber;

	public Year() {
	}

	public Year(int yearNumber) {
		this.yearNumber = yearNumber;
	}

	public int getYearNumber() {
		return yearNumber;
	}

	public void setYearNumber(int yearNumber) {
		this.yearNumber = yearNumber;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + yearNumber;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Year other = (Year) obj;
		if (yearNumber != other.yearNumber)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return Integer.toString(yearNumber);
	}
	
	


	

	
}
