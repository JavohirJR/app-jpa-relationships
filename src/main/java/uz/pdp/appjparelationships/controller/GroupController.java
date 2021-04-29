package uz.pdp.appjparelationships.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import uz.pdp.appjparelationships.entity.Faculty;
import uz.pdp.appjparelationships.entity.Group;
import uz.pdp.appjparelationships.entity.Subject;
import uz.pdp.appjparelationships.payload.GroupDto;
import uz.pdp.appjparelationships.repository.FacultyRepository;
import uz.pdp.appjparelationships.repository.GroupRepository;
import uz.pdp.appjparelationships.repository.SubjectRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/group")
public class GroupController {

    @Autowired
    GroupRepository groupRepository;

    @Autowired
    FacultyRepository facultyRepository;

    @Autowired
    SubjectRepository subjectRepository;

    //VAZIRLIK UCHUN
    //READ

    @GetMapping
    public List<Group> getGroups() {
        return groupRepository.findAll();
    }


    //UNIVERSITET MAS'UL XODIMI UCHUN
    @GetMapping("/byUniversityId/{universityId}")
    public List<Group> getGroupsByUniversityId(@PathVariable Integer universityId) {
        return groupRepository.findAllByFaculty_UniversityId(universityId);
    }

    @PostMapping
    public String addGroup(@RequestBody GroupDto groupDTO) {

        Optional<Faculty> optionalFaculty = facultyRepository.findById(groupDTO.getFacultyId());
        if (!optionalFaculty.isPresent()) {
            return "Such faculty not found";
        }
        List<Subject> subjects = new ArrayList<>();
        Integer[] subjectList = groupDTO.getSubjectList();
        for (Integer integer : subjectList) {
            Optional<Subject> byId1 = subjectRepository.findById(integer);
            if (!byId1.isPresent()){
                return "Subject is not found";
            }
            subjects.add(byId1.get());
        }
        Group group = new Group();
        group.setName(groupDTO.getName());
        group.setFaculty(facultyRepository.getOne(groupDTO.getFacultyId()));
        group.setSubjects(subjects);
        groupRepository.save(group);
        return "The group added successfully!";}
}
