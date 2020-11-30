package cn.booktable.activiti.entity.activiti;

import lombok.Data;

import java.util.List;

/**
 * @author ljc
 */
@Data
public class ActTimeline {
    private String id;
    private String userId;
    private String taskId;
    private String name;
    private String type;
    private List<String> users;
    private List<String> groups;
    private List<ActTimeline> outgoing;
    private List<ActComment> comments;
}
