package com.code.generator;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.code.generator.service.CodeGeneratorService;

@RunWith(SpringRunner.class)
@SpringBootTest
public class GeneratorApplicationTests {

	@Autowired
	private CodeGeneratorService generatorService;

	@Test
	public void contextLoads() {
		generatorService.generatorModel();
	}

}
