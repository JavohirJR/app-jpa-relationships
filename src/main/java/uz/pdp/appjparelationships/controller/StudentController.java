package uz.pdp.appjparelationships.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;
import uz.pdp.appjparelationships.entity.Address;
import uz.pdp.appjparelationships.entity.Group;
import uz.pdp.appjparelationships.entity.Student;
import uz.pdp.appjparelationships.payload.StudentDTO;
import uz.pdp.appjparelationships.repository.AddressRepository;
import uz.pdp.appjparelationships.repository.GroupRepository;
import uz.pdp.appjparelationships.repository.StudentRepository;

import java.util.Objects;
import java.util.Optional;

@RestController
@RequestMapping("/student")
public class StudentController {
    @Autowired
    StudentRepository studentRepository;

    @Autowired
    GroupRepository groupRepository;

    @Autowired
    AddressRepository addressRepository;

    //1. VAZIRLIK
    @GetMapping("/forMinistry")
    public Page<Student> getStudentListForMinistry(@RequestParam int page) {
        Pageable pageable = PageRequest.of(page, 10);
        return studentRepository.findAll(pageable);
    }

    //2. UNIVERSITY
    @GetMapping("/forUniversity/{universityId}")
    public Page<Student> getStudentListForUniversity(@PathVariable Integer universityId,
                                                     @RequestParam int page) {
        Pageable pageable = PageRequest.of(page, 10);
        return studentRepository.findAllByGroup_Faculty_UniversityId(universityId, pageable);
    }

    //3. FACULTY DEKANAT
    @GetMapping(value = "/forFaculty/{facId}")
    public Page<Student> getStudentsForFaculty(@PathVariable Integer facId,
                                               @RequestParam int page) {
        Pageable pageable = PageRequest.of(page, 10);
        return studentRepository.findAllByGroup_Faculty_Id(facId, pageable);
    }

    //4. GROUP OWNER
    @GetMapping(value = "/forGroup/{groupId}")
    public Page<Student> getStudentsForGroup(@PathVariable Integer groupId,
                                             @RequestParam int page) {
        Pageable pageable = PageRequest.of(page, 10);
        return studentRepository.findAllByGroup_Id(groupId, pageable);
    }

    @GetMapping("/{id}")
    public Student getOne(@PathVariable Integer id) {
        Optional<Student> optionalStudent = studentRepository.findById(id);
        return optionalStudent.orElseGet(Student::new);
    }

    @PostMapping
    public String addOne(@RequestBody StudentDTO studentDTO) {
        Optional<Group> optionalGroup = groupRepository.findById(studentDTO.getGroupId());
        if (!optionalGroup.isPresent()) return "Group is not found!";
        if (studentRepository.existsByFirstNameAndLastNameAndGroup_Id(studentDTO.getFirstName(), studentDTO.getLastName(), studentDTO.getGroupId())) {
            return "Student is already exist";
        }
        Address address = new Address();
        address.setCity(studentDTO.getCity());
        address.setDistrict(studentDTO.getDistrict());
        address.setStreet(studentDTO.getStreet());
        Address savedAddress = addressRepository.save(address);

        Student student = new Student();
        student.setFirstName(studentDTO.getFirstName());
        student.setLastName(studentDTO.getLastName());
        student.setGroup(optionalGroup.get());
        student.setAddress(savedAddress);

        studentRepository.save(student);
        return "Student added successfully";
    }

    @PutMapping("/{id}")
    public String editOne(@PathVariable Integer id, @RequestBody StudentDTO studentDTO) {
        Optional<Group> optionalGroup = groupRepository.findById(studentDTO.getGroupId());
        if (!optionalGroup.isPresent()) return "Group is not found";

        Optional<Student> optionalStudent = studentRepository.findById(id);
        if (!optionalStudent.isPresent()) return "Student is not found";

        Student student = studentRepository.getOne(id);

        Address addressExist = new Address(student.getAddress().getId(),studentDTO.getCity(), studentDTO.getDistrict(), studentDTO.getStreet());

        boolean exists = studentRepository.existsByFirstNameAndLastNameAndGroup_Id(studentDTO.getFirstName(), studentDTO.getLastName(), studentDTO.getGroupId());
        if (exists&& Objects.equals(student.getAddress(), addressExist)) {
            return "Student is already exist";
        }
        if (!student.getFirstName().equals(studentDTO.getFirstName())) {
            student.setFirstName(studentDTO.getFirstName());
        }
        if (!student.getLastName().equals(studentDTO.getLastName())) {
            student.setLastName(studentDTO.getLastName());
        }
        if (!student.getGroup().equals(groupRepository.getOne(studentDTO.getGroupId()))) {
            student.setGroup(optionalGroup.get());
        }

        Address address = student.getAddress();
        address.setStreet(studentDTO.getStreet());
        address.setDistrict(studentDTO.getDistrict());
        address.setCity(studentDTO.getCity());

        addressRepository.save(address);
        studentRepository.save(student);
        return "Student edited successfully";

    }

    @DeleteMapping("/{id}")
    public String deleteOne(@PathVariable Integer id) {
        if (studentRepository.existsById(id)) {
            studentRepository.deleteById(id);
            return "Student deleted successfully";
        }
        return "Student is not found";
    }
}
