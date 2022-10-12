package com.example.demo.model.test;/*
 * Copyright © 2020-2035 erupt.xyz All rights reserved.
 * Author: YuePeng (erupts@126.com)
 */

import javax.persistence.*;
import xyz.erupt.annotation.*;
import xyz.erupt.annotation.sub_field.*;
import xyz.erupt.annotation.sub_field.sub_edit.*;
import xyz.erupt.jpa.model.BaseModel;
import java.util.Set;

@Erupt(name = "test1012")
@Table(name = "test_1012")
@Entity
public class Test1012 extends BaseModel {

        @EruptField(
                views = @View(
                        title = "my_key"
                ),
                edit = @Edit(
                        title = "my_key",
                        type = EditType.TAB_TABLE_ADD, search = @Search, notNull = true
                )
        )
        @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
        @OrderBy
        @JoinColumn(name = "test1012_id") 
        private Set<TestDict> my_key;

}