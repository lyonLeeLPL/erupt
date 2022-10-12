package com.example.demo.model.test;

import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;
import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.EruptI18n;
import xyz.erupt.annotation.constant.AnnotationConst;
import xyz.erupt.annotation.sub_erupt.Power;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.EditType;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.annotation.sub_field.sub_edit.BoolType;
import xyz.erupt.annotation.sub_field.sub_edit.Search;
import xyz.erupt.upms.helper.HyperModelUpdateVo;

import javax.persistence.*;
import java.util.Set;

/**
 * @author YuePeng
 * date 2018-11-22.
 */
@Entity
@Table(name = "test_1009")

@Erupt(
        name = "test1009",
        power = @Power(export = true)
)
@EruptI18n
@Getter
@Setter
@Component
public class Test1009 extends HyperModelUpdateVo {

        @Column(length = AnnotationConst.CODE_LENGTH)
        @EruptField(
                views = @View(title = "编码"),
                edit = @Edit(title = "编码", notNull = true, search = @Search(vague = true))
        )
        private String code;

        @EruptField(
                views = @View(title = "名称"),
                edit = @Edit(title = "名称", notNull = true, search = @Search(vague = true))
        )
        private String name;

        @EruptField(
                views = @View(title = "展示顺序", sortable = true),
                edit = @Edit(title = "展示顺序", desc = "数值越小，越靠前")
        )
        private Integer sort;

        @EruptField(
                views = @View(title = "状态", sortable = true),
                edit = @Edit(
                        title = "状态",
                        type = EditType.BOOLEAN,
                        notNull = true,
                        search = @Search(vague = true),
                        boolType = @BoolType(trueText = "启用", falseText = "禁用")
                )
        )
        private Boolean status = true;


        @OneToMany(cascade = CascadeType.ALL)
        @JoinColumn(name = "test_dict")
        @EruptField(
                edit = @Edit(title = "一对多新增", type = EditType.TAB_TABLE_ADD)
        )
        private Set<TestDict> dict;


}
