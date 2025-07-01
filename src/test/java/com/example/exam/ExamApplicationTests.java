package com.example.exam;

import com.example.exam.pojo.*;
import com.example.exam.service.RecognitionService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;


@SpringBootTest
class ExamApplicationTests {

    private final RecognitionService service = new RecognitionService();

    // 无交易的测试用例
    @Test
    void testNoTransaction() {
        List<Layer> open = layers(100, 200, 300, 400, 500, 600, 700, 800, 900, 1000);
        List<Layer> close = layers(100, 200, 300, 400, 500, 600, 700, 800, 900, 1000);

        RecognitionResult result = service.recognition(open, close, List.of(), List.of(), 5);

        assertTrue(result.isSuccessful());
        assertTrue(result.getItems().isEmpty());
        assertTrue(result.getExceptions().isEmpty());
    }

    // 单个物品被识别的测试用例
    @Test
    void testSingleItemTaken() {
        Goods g1 = new Goods("A", 100, 0);
        Stock s1 = new Stock("A", 1, 10);

        List<Layer> open = layers(500, 200, 300, 400, 500, 600, 700, 800, 900, 1000);
        List<Layer> close = layers(400, 200, 300, 400, 500, 600, 700, 800, 900, 1000);

        RecognitionResult result = service.recognition(open, close, List.of(g1), List.of(s1), 5);

        assertTrue(result.isSuccessful());
        assertEquals(1, result.getItems().size());
        assertEquals("A", result.getItems().get(0).getGoodsId());
        assertEquals(1, result.getItems().get(0).getNum());
    }

    // 识别放了物品进货柜的测试用例
    @Test
    void testPutItemBackAsForeignObject() {
        List<Layer> open = layers(100, 200, 300, 400, 500, 600, 700, 800, 900, 1000);
        List<Layer> close = layers(110, 200, 300, 400, 500, 600, 700, 800, 900, 1000); // 第一层多了 10g

        RecognitionResult result = service.recognition(open, close, List.of(), List.of(), 5);

        assertFalse(result.isSuccessful());
        assertEquals(1, result.getExceptions().size());
        assertEquals(ExceptionEnum.FOREIGN_OBJECT, result.getExceptions().get(0).getException());
    }

    //组合商品识别测试用例
    @Test
    void testComboRecognition() {
        Goods g1 = new Goods("A", 100, 5);
        Goods g2 = new Goods("B", 200, 5);
        Stock s1 = new Stock("A", 1, 10);
        Stock s2 = new Stock("B", 1, 10);

        List<Layer> open = layers(600, 200, 300, 400, 500, 600, 700, 800, 900, 1000);
        List<Layer> close = layers(300, 200, 300, 400, 500, 600, 700, 800, 900, 1000); // 减少 300g

        RecognitionResult result = service.recognition(open, close, List.of(g1, g2), List.of(s1, s2), 5);

        assertFalse(result.isSuccessful());
        assertEquals(1, result.getExceptions().size());
        assertEquals(ExceptionEnum.UNRECOGNIZABLE, result.getExceptions().get(0).getException());
    }

    private List<Layer> layers(int... weights) {
        return Arrays.stream(weights)
                .mapToObj(w -> {
                    Layer l = new Layer();
                    l.setWeight(w);
                    return l;
                })
                .toList();
    }
}
