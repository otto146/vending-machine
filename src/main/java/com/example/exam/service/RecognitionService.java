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
                                         List<Stock> stockList,
                                         int sensorTolerance) {


        RecognitionResult recognitionResult = new RecognitionResult();


        //最终结果集合
        List<RecognitionItem> recognitionItems = new ArrayList<>();

        //识别异常集合
        List<RecognitionException> recognitionExceptions = new ArrayList<>();


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
        //如果差值小于容差则认为没出货
        int totalDelta = totalOpenLayersWeight - totalCloseLayersWeight;
        if (Math.abs(totalDelta) <= sensorTolerance) {
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

            //开门时重量减去关门时重量
            int delta = beginWeight - endWeight;
            //添加容差处理，负号就是左边界，如果差值比左边界小说明关门的时候的重量比开门的时候的大，也就是往里面放东西了
            if (delta < -sensorTolerance) {
                recognitionExceptions.add(
                        new RecognitionException(layer, ExceptionEnum.FOREIGN_OBJECT, beginWeight, endWeight));
                continue;
            }
            //差值的绝对值小于等于容差识别为无购物
            if (Math.abs(delta) <= sensorTolerance) {
                continue;
            }

            //差值大于0说明用户取东西了
            //因为层架商品少而且重量会刻意差异较大，所以可以直接穷举商品组合
            //先试试单个商品的，从1到库存大小
            //用差值去减商品重量，如果为0说明找到商品了
            //当前的层架编号i，先根据stock找到商品id

            //先来个集合存当前层架的Stock
            List<Stock> currentLayerStocks = new ArrayList<>();

            //识别出来的单商品集合
            List<RecognitionItem> SingleRecognitionItems = new ArrayList<>();

            //识别出来的组合商品集合
            List<Map<String, Integer>> validCombos = new ArrayList<>();

            for (Stock stock : stockList) {
                //如果层架编号相同就加入到集合中
                if (stock.getLayer() == layer) {
                    currentLayerStocks.add(stock);
                }
            }

            //现在可以尝试单个商品匹配delta
            for (Stock stock : currentLayerStocks) {
                //现在我们拿到商品ID和对应的库存了
                String goodsId = stock.getGoodsId();
                int maxCount = stock.getNum();
                // 查找商品的重量
                int unitWeight = 0;
                for (Goods goods : goodsList) {
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

                        // 加入单商品识别结果
                        SingleRecognitionItems.add(new RecognitionItem(goodsId, count));
                        break;
                    }
                }
            }

            double targetMin = delta - sensorTolerance;
            double targetMax = delta + sensorTolerance;

            // 单个商品识别失败，尝试组合识别

            findCombinations(currentLayerStocks, goodsList, 0, new HashMap<>(), validCombos, targetMin, targetMax,
                    0, 0);
            //如果只有一种组合就认为识别成功

            Map<String, Integer> mergedItemsMap = new HashMap<>();

            if (SingleRecognitionItems.size() == 1 && validCombos.isEmpty()) {
                // 只有单商品结果，放入结果
                for (RecognitionItem item : SingleRecognitionItems) {
                    mergedItemsMap.put(item.getGoodsId(),
                            mergedItemsMap.getOrDefault(item.getGoodsId(), 0) + item.getNum());
                }
            } else if (validCombos.size() == 1 && SingleRecognitionItems.isEmpty()) {
                // 只有组合结果，放入结果
                for (Map.Entry<String, Integer> entry : validCombos.get(0).entrySet()) {
                    mergedItemsMap.put(entry.getKey(),
                            mergedItemsMap.getOrDefault(entry.getKey(), 0) + entry.getValue());
                }
            } else {
                // 两者都有解或者两者都没解，算异常
                recognitionExceptions.add(
                        new RecognitionException(layer, ExceptionEnum.UNRECOGNIZABLE, beginWeight, endWeight)
                );
            }

            if (!mergedItemsMap.isEmpty()) {
                for (Map.Entry<String, Integer> entry : mergedItemsMap.entrySet()) {
                    recognitionItems.add(new RecognitionItem(entry.getKey(), entry.getValue()));
                }
            }
            System.out.println("SingleRecognitionItems size: " + SingleRecognitionItems.size());
            System.out.println("validCombos size: " + validCombos.size());

        }

        recognitionResult.setItems(recognitionItems);
        recognitionResult.setExceptions(recognitionExceptions);
        recognitionResult.setSuccessful(recognitionExceptions.isEmpty());
        return recognitionResult;
    }

    //组合识别
    private void findCombinations(List<Stock> stockList, List<Goods> goodsList,
                                  //index表示当前商品的索引
                                  int index,
                                  Map<String, Integer> currentCombo,//商品的组合集合
                                  List<Map<String, Integer>> validCombos,//有效的商品组合集合
                                  //目标区间
                                  double targetMin, double targetMax,
                                  //组合区间
                                  double comboMin, double comboMax) {

        // 遍历完全部商品再开始判断
        if (index >= stockList.size()) {
            // 当前组合区间与目标区间有交集
            if (comboMax >= targetMin && comboMin <= targetMax && currentCombo.size() >= 2) {
                validCombos.add(new HashMap<>(currentCombo));
            }
            return;
        }
        Stock stock = stockList.get(index);
        String goodsId = stock.getGoodsId();
        int maxCount = stock.getNum();

        Goods matched = null;
        for (Goods g : goodsList) {
            if (g.getId().equals(goodsId)) {
                matched = g;
                break;
            }
        }
        if (matched == null) return;
        //考虑包装误差计算当前商品的最大最小重量
        double unitMin = matched.getWeight() * (1 - matched.getPackageTolerance() / 100.0);
        double unitMax = matched.getWeight() * (1 + matched.getPackageTolerance() / 100.0);

        for (int count = 1; count <= maxCount; count++) {
            double addMin = count * unitMin;
            double addMax = count * unitMax;
            //将当前商品加入组合
            currentCombo.put(goodsId, count);
            //递归处理下一个商品
            findCombinations(stockList, goodsList, index + 1,
                    currentCombo, validCombos,
                    targetMin, targetMax,
                    comboMin + addMin, comboMax + addMax);
            //将当前商品从组合中移除
            currentCombo.remove(goodsId);

            if (validCombos.size() > 1) return; // 多解则提前终止
        }
    }
}
