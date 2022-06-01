package edu.ucsb.cs156.courses.entities;
import javax.persistence.Entity;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.GeneratedValue;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity(name = "personalcourses")
public class PersonalCourses {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  // public String getEnrollCd(){return enrollCd;}
  private long id;
  private long psId;
  private String enrollCd;
  private String quarter;
}
