package tables;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;

@Entity
@Table(name="examinationCards")
public class ExaminationCard implements Serializable{
	
	
	@Transient
	private static final long serialVersionUID = 8729595795492394112L;

	@Id
	@SequenceGenerator(name="examinationCardsSeq", sequenceName="examination_cards_Seq", allocationSize=1, initialValue=1)
	@GeneratedValue(strategy=GenerationType.SEQUENCE, generator="examinationCardsSeq")
	@Column(name="examcard_id")
	private int examCardId;
	
	@ManyToOne
	@JoinColumn(name="car_id")
	private Car carNumber;
	
	@ManyToOne(fetch=FetchType.EAGER)
	@JoinColumn(name="category_id")
	private ExamCategory category;
	
	@Column(name="comments")
	private String comments;
	
	@Column(name="result")
	private String result;
	
	@ManyToOne(fetch=FetchType.EAGER)
	@JoinColumn(name="term_id")
	private Term term;
	
	@ManyToOne(fetch=FetchType.EAGER)
	@JoinColumn(name="resources_id")
	private Resource resource;
	
	@ManyToOne(fetch=FetchType.EAGER)
	@JoinColumn(name="examiner")
	private User examiner;
	
	@ManyToOne(fetch=FetchType.EAGER)
	@JoinColumn(name="examined")
	private User examined;
	
	@OneToMany(mappedBy="examCard", cascade=CascadeType.ALL)
	private List<ExamTaskResult> examTaskResults = new ArrayList<>();
	
	public ExaminationCard() {
		
	}

	public ExaminationCard(ExamCategory category, Car carNumber, String comments, String result, Term term, Resource resource, User examiner,
			User examined) {
		super();
		this.carNumber = carNumber;
		this.category = category;
		this.comments = comments;
		this.result = result;
		this.term = term;
		this.resource = resource;
		this.examiner = examiner;
		this.examined = examined;
	}
	
	public void addTaskResult(ExamTaskResult task) {
		this.examTaskResults.add(task);
    }
    
	public int getExamCardId() {
		return examCardId;
	}

	public void setExamCardId(int examCardId) {
		this.examCardId = examCardId;
	}
	
	public Car getCarNumber() {
		return carNumber;
	}

	public void setCarNumber(Car carNumber) {
		this.carNumber = carNumber;
	}

	public ExamCategory getCategory() {
		return category;
	}

	public void setCategory(ExamCategory category) {
		this.category = category;
	}

	public String getComments() {
		return comments;
	}

	public void setComments(String comments) {
		this.comments = comments;
	}

	public String getResult() {
		return result;
	}

	public void setResult(String result) {
		this.result = result;
	}

	public Term getTerm() {
		return term;
	}

	public void setTerm(Term term) {
		this.term = term;
	}

	public Resource getResource() {
		return resource;
	}

	public void setResource(Resource resource) {
		this.resource = resource;
	}

	public User getExaminer() {
		return examiner;
	}

	public void setExaminer(User examiner) {
		this.examiner = examiner;
	}

	public User getExamined() {
		return examined;
	}

	public void setExamined(User examined) {
		this.examined = examined;
	}

	public List<ExamTaskResult> getExamTaskResults() {
		return examTaskResults;
	}

	public void setExamTaskResults(List<ExamTaskResult> examTaskResults) {
		this.examTaskResults = examTaskResults;
	}

	@Override
	public String toString() {
		return "ExaminationCard [examCardId=" + examCardId + ", category=" + category + ", comments=" + comments
				+ ", result=" + result + ", term=" + term + ", resource=" + resource + ", examiner=" + examiner
				+ ", examined=" + examined + "]";
	}
	
	
	
	
	
}
