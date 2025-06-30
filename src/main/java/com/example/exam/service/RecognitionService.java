package com.example.exam.service;

import com.example.exam.pojo.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RecognitionService {

    public RecognitionResult recognition(List<Layer> openLayers,
                                         List<Layer> closeLayers,
                                         List<Goods> goodsList,
                                         List<Stock> stockList) {


        RecognitionResult recognitionResult = new RecognitionResult();

        //为了保证不重复但不需要保证有序，用map去接数据
        Map<Integer, Integer> openLayersMap = new HashMap<>();
        Map<Integer, Integer> closeLayersMap = new HashMap<>();
        //识别出来的商品集合
        List<RecognitionItem> recognitionItems = new ArrayList<>();
        //识别异常集合
        List<RecognitionException> recognitionExceptions = new ArrayList<>();

        recognitionResult.setItems(recognitionItems);
        recognitionResult.setExceptions(recognitionExceptions);

        int totalOpenLayersWeight = 0;
        for (int i = 0; i < 10; i++) {
            totalOpenLayersWeight += openLayers.get(i).getWeight();
        }

        int totalCloseLayersWeight = 0;
        for (int i = 0; i < 10; i++) {
            totalCloseLayersWeight += closeLayers.get(i).getWeight();
        }


        //可以先判断开门前后的总重量对比，相同说明没出货，不同则说明出货了
        //相同的情况
        if (totalOpenLayersWeight == totalCloseLayersWeight) {
            recognitionResult.setSuccessful(true);
            return recognitionResult;
        }

        //不同的话就说明出货，我们就循环查找重量变化的货架
        for (int i = 0; i < 10; i++) {
            //每一层的前后重量
            int beginWeight = openLayers.get(i).getWeight();
            int endWeight = closeLayers.get(i).getWeight();
            //层架编号
            int layer = i + 1;
            //识别结果
            boolean matched = false;
            //开门时重量减去关门时重量
            int delta = beginWeight - endWeight;
            if (delta < 0) {
                recognitionExceptions.add(
                        new RecognitionException(layer, ExceptionEnum.FOREIGN_OBJECT, beginWeight, endWeight));
                continue;
            }
            if (delta == 0) {
                continue;
            }

            //差值大于0说明用户取东西了
            //因为层架商品少而且重量会刻意差异较大，所以可以直接穷举商品组合
            //先试试单个商品的，从1到库存大小
            //用差值去减商品重量，如果为0说明找到商品了
            //当前的层架编号i，先根据stock找到商品id
            //先来个集合存当前层架的Stock
            List<Stock> currentLayerStocks = new ArrayList<>();
            for (int j = 0; j < stockList.size(); j++) {
                Stock stock = stockList.get(j);
                //如果层架编号相同就加入到集合中
                if (stock.getLayer() == layer) {
                    currentLayerStocks.add(stock);
                }
            }
            //现在可以尝试单个商品匹配delta
            for (int k = 0; k < currentLayerStocks.size(); k++) {
                Stock stock = currentLayerStocks.get(k);
                //现在我们拿到商品ID和对应的库存了
                String goodsId = stock.getGoodsId();
                int maxCount = stock.getNum();
                // 查找商品的重量
                int unitWeight = 0;
                for (int g = 0; g < goodsList.size(); g++) {
                    Goods goods = goodsList.get(g);
                    if (goods.getId().equals(goodsId)) {
                        unitWeight = goods.getWeight();
                        //找到商品了就不要循环下去了不然就覆盖了
                        break;
                    }
                }

                // 试着从 1 到 maxCount 的商品数量，看能不能刚好匹配 delta
                for (int count = 1; count <= maxCount; count++) {
                    if (unitWeight * count == delta) {
                        // 说明识别成功，用户在当前层拿了 count 个 goodsId 商品
                        System.out.println("用户在第 " + layer + " 层拿了 " + count + " 个商品：" + goodsId);

                        // 加入识别结果（RecognitionItem）
                        recognitionItems.add(new RecognitionItem(goodsId, count));
                        matched = true;
                        break;
                    }
                }
                if (matched) break; // 找到了就不试别的商品了
            }
            // 如果没有匹配成功，说明无法识别（可能是组合、可能是误差），记异常
            if (!matched) {
                recognitionExceptions.add(
                        new RecognitionException(layer, ExceptionEnum.UNRECOGNIZABLE, openLayers.get(i).getWeight(), closeLayers.get(i).getWeight())
                );
            }
        }
        recognitionResult.setSuccessful(recognitionExceptions.isEmpty());
        return recognitionResult;
    }

}




