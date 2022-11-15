package com.example.demo.model.test;/*
 * Copyright © 2020-2035 erupt.xyz All rights reserved.
 * Author: YuePeng (erupts@126.com)
 */

import javax.persistence.*;
import xyz.erupt.annotation.*;
import xyz.erupt.annotation.sub_field.*;
import xyz.erupt.annotation.sub_field.sub_edit.*;
import xyz.erupt.jpa.model.BaseModel;

@Erupt(name = "testDict")
@Table(name = "test_dict")
@Entity
public class TestDict extends BaseModel {

        @EruptField(
                views = @View(
                        title = "my_key"
                ),
                edit = @Edit(
                        title = "my_key",
                        type = EditType.INPUT, search = @Search, notNull = true,
                        inputType = @InputType
                )
        )
        private String my_key;

}