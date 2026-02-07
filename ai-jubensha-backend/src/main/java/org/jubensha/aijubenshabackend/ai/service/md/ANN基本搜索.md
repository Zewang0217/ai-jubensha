# 基本向量搜索 | Milvus 文档

[🚀 Zilliz 云：全面管理 Milvus - 速度快 10 倍。零烦恼。专为人工智能打造。立即免费试用 →](https://cloud.zilliz.com/signup?utm_source=milvusio&utm_medium=referral&utm_campaign=milvus_top_banner&utm_content=docs/zh/single-vector-search.md)

[![Milvus](/images/layout/milvus-logo.svg)](/zh)

[![Zilliz](/images/layout/zilliz-logo.svg)](https://zilliz.com/)

-   为什么选择 Milvus

    -   [什么是 Milvus](/zh/intro)
    -   [使用案例](/zh/use-cases)

-   [文档](/docs/zh)
-   教程

    -   [训练营](/zh/bootcamp)
    -   [演示](/zh/milvus-demos)
    -   [视频](https://www.youtube.com/c/MilvusVectorDatabase)

-   工具

    -   [Attu](https://github.com/zilliztech/attu)
    -   [Milvus CLI](https://github.com/zilliztech/milvus_cli)
    -   [配置工具](/zh/tools/sizing)
    -   [Milvus 备份](https://github.com/zilliztech/milvus-backup)
    -   [VTS](https://github.com/zilliztech/vts)
    -   [深度搜索器](https://github.com/zilliztech/deep-searcher)
    -   [Claude Context](https://github.com/zilliztech/claude-context)

-   [博客](/zh/blog)
-   社区

    -   [米尔沃斯办公时间](https://meetings.hubspot.com/chloe-williams1/milvus-office-hour?uuid=4cb203e5-482a-47e0-90a6-7acc511d61f4)
    -   [松弛](https://milvus.io/slack)
    -   [Discord](https://milvus.io/discord)
    -   [GitHub](https://github.com/milvus-io/milvus/discussions)
    -   [更多社区](/zh/community)


简体中文

[Star42.6K](https://github.com/milvus-io/milvus)[预约演示](/zh/contact)[

开始使用

](https://cloud.zilliz.com/signup?utm_source=milvusio&utm_medium=referral&utm_campaign=milvus_nav_right&utm_content=docs/zh/single-vector-search.md)

[![Milvus](/images/layout/milvus-logo.svg)](/zh)

[![Zilliz](/images/layout/zilliz-logo.svg)](https://zilliz.com/)

简体中文

搜索K

[首页](/docs/zh)

[v2.6.x](/docs/single-vector-search.md)

​

-   关于 Milvus

-   开始

-   概念

-   用户指南

    -   [数据库](/docs/zh/manage_databases.md)

    -   Collections

    -   Schema 和数据字段

    -   插入和删除

    -   索引

    -   搜索

        -   [基本 ANN 搜索](/docs/zh/single-vector-search.md)

        -   [过滤搜索](/docs/zh/filtered-search.md)

        -   [范围搜索](/docs/zh/range-search.md)

        -   [分组搜索](/docs/zh/grouping-search.md)

        -   [主键搜索](/docs/zh/primary-key-search.md)

        -   [混合搜索](/docs/zh/multi-vector-search.md)

        -   [查询](/docs/zh/get-and-scalar-query.md)

        -   过滤

        -   [全文检索](/docs/zh/full-text-search.md)

        -   [文本匹配](/docs/zh/keyword-match.md)

        -   [文本高亮显示](/docs/zh/text-highlighter.md)

        -   [短语匹配](/docs/zh/phrase-match.md)

        -   [使用 Embeddings 列表搜索](/docs/zh/search-with-embedding-lists.md)

        -   [将 Elasticsearch 查询转至 Milvus](/docs/zh/elasticsearch-queries-to-milvus.md)

        -   [搜索迭代器](/docs/zh/with-iterators.md)

        -   [使用 Partition Key](/docs/zh/use-partition-key.md)


    -   Embeddings &amp; Rerankers
        
    -   存储优化


-   数据导入

-   管理指南

-   工具

-   集成

-   教程

-   常见问题

-   API Reference


-   [首页](/zh)
-   [文档](/docs/zh)
-   用户指南

-   搜索

-   基本 ANN 搜索


# 基本向量搜索

近似近邻（ANN）搜索以记录向量嵌入排序顺序的索引文件为基础，根据接收到的搜索请求中携带的查询向量查找向量嵌入子集，将查询向量与子群中的向量进行比较，并返回最相似的结果。通过 ANN 搜索，Milvus 提供了高效的搜索体验。本页将帮助您了解如何进行基本的 ANN 搜索。

如果在创建 Collections 后动态添加新字段，包含这些字段的搜索将返回已定义的默认值，对于未明确设置值的实体，则返回 NULL。有关详细信息，请参阅[向现有 Collections 添加字段](/docs/zh/add-fields-to-an-existing-collection.md)。

## 概述

ANN 和 k-Nearest Neighbors (kNN) 搜索是向量相似性搜索的常用方法。在 kNN 搜索中，必须将向量空间中的所有向量与搜索请求中携带的查询向量进行比较，然后找出最相似的向量，这既耗时又耗费资源。

与 kNN 搜索不同，ANN 搜索算法要求提供一个**索引**文件，记录向量 Embeddings 的排序顺序。当收到搜索请求时，可以使用索引文件作为参考，快速找到可能包含与查询向量最相似的向量嵌入的子组。然后，你可以使用指定的**度量类型**来测量查询向量与子组中的向量之间的相似度，根据与查询向量的相似度对组成员进行排序，并找出**前 K 个**组成员。

ANN 搜索依赖于预建索引，搜索吞吐量、内存使用量和搜索正确性可能会因选择的索引类型而不同。您需要在搜索性能和正确性之间取得平衡。

为了减少学习曲线，Milvus 提供了**AUTOINDEX**。通过**AUTOINDEX**，Milvus 可以在建立索引的同时分析 Collections 中的数据分布，并根据分析结果设置最优化的索引参数，从而在搜索性能和正确性之间取得平衡。

在本节中，你将找到有关以下主题的详细信息：

-   [单向量搜索](/docs/zh/single-vector-search.md#Single-Vector-Search)

-   [批量向量搜索](/docs/zh/single-vector-search.md#Bulk-Vector-Search)

-   [分区中的 ANN 搜索](/docs/zh/single-vector-search.md#ANN-Search-in-Partition)

-   [使用输出字段](/docs/zh/single-vector-search.md#Use-Output-Fields)

-   [使用限制和偏移](/docs/zh/single-vector-search.md#Use-Limit-and-Offset)

-   [使用级别](/docs/zh/single-vector-search.md#Use-Level)

-   [获取召回率](/docs/zh/single-vector-search.md#Get-Recall-Rate)

-   [增强 ANN 搜索](/docs/zh/single-vector-search.md#Enhancing-ANN-Search)


## 单向量搜索

在 ANN 搜索中，单向量搜索指的是只涉及一个查询向量的搜索。根据预建索引和搜索请求中携带的度量类型，Milvus 将找到与查询向量最相似的前 K 个向量。

本节将介绍如何进行单向量搜索。搜索请求携带单个查询向量，要求 Milvus 使用内积（IP）计算查询向量与 Collections 中向量的相似度，并返回三个最相似的向量。

[Python](#python) [Java](#java) [Go](#go) [NodeJS](#javascript) [cURL](#bash)

```python
from pymilvus import MilvusClient

client = MilvusClient(
    uri="http://localhost:19530",
    token="root:Milvus"
)

# 4. Single vector search
query_vector = [0.3580376395471989, -0.6023495712049978, 0.18414012509913835, -0.26286205330961354, 0.9029438446296592]
res = client.search(
    collection_name="quick_setup",
    anns_field="vector",
    data=[query_vector],
    limit=3,
    search_params={"metric_type": "IP"}
)

for hits in res:
    for hit in hits:
        print(hit)

# [
#     [
#         {
#             "id": 551,
#             "distance": 0.08821295201778412,
#             "entity": {}
#         },
#         {
#             "id": 296,
#             "distance": 0.0800950899720192,
#             "entity": {}
#         },
#         {
#             "id": 43,
#             "distance": 0.07794742286205292,
#             "entity": {}
#         }
#     ]
# ]

  
    
  
```

```java
import io.milvus.v2.client.ConnectConfig;
import io.milvus.v2.client.MilvusClientV2;
import io.milvus.v2.service.vector.request.SearchReq;
import io.milvus.v2.service.vector.request.data.FloatVec;
import io.milvus.v2.service.vector.response.SearchResp;

import java.util.*;

MilvusClientV2 client = new MilvusClientV2(ConnectConfig.builder()
        .uri("http://localhost:19530")
        .token("root:Milvus")
        .build());
    
FloatVec queryVector = new FloatVec(new float[]{0.3580376395471989f, -0.6023495712049978f, 0.18414012509913835f, -0.26286205330961354f, 0.9029438446296592f});
SearchReq searchReq = SearchReq.builder()
        .collectionName("quick_setup")
        .data(Collections.singletonList(queryVector))
        .annsField("vector")
        .topK(3)
        .build();

SearchResp searchResp = client.search(searchReq);

List<List<SearchResp.SearchResult>> searchResults = searchResp.getSearchResults();
for (List<SearchResp.SearchResult> results : searchResults) {
    System.out.println("TopK results:");
    for (SearchResp.SearchResult result : results) {
        System.out.println(result);
    }
}

// Output
// TopK results:
// SearchResp.SearchResult(entity={}, score=0.95944905, id=5)
// SearchResp.SearchResult(entity={}, score=0.8689616, id=1)
// SearchResp.SearchResult(entity={}, score=0.866088, id=7)

  
    
  
```

```go
import (
    "context"
    "fmt"

    "github.com/milvus-io/milvus/client/v2/entity"
    "github.com/milvus-io/milvus/client/v2/milvusclient"
)

ctx, cancel := context.WithCancel(context.Background())
defer cancel()

milvusAddr := "localhost:19530"
token := "root:Milvus"

client, err := milvusclient.New(ctx, &milvusclient.ClientConfig{
    Address: milvusAddr,
    APIKey:  token,
})
if err != nil {
    fmt.Println(err.Error())
    // handle error
}
defer client.Close(ctx)

queryVector := []float32{0.3580376395471989, -0.6023495712049978, 0.18414012509913835, -0.26286205330961354, 0.9029438446296592}

resultSets, err := client.Search(ctx, milvusclient.NewSearchOption(
    "quick_setup", // collectionName
    3,               // limit
    []entity.Vector{entity.FloatVector(queryVector)},
).WithANNSField("vector"))
if err != nil {
    fmt.Println(err.Error())
    // handle error
}

for _, resultSet := range resultSets {
    fmt.Println("IDs: ", resultSet.IDs.FieldData().GetScalars())
    fmt.Println("Scores: ", resultSet.Scores)
}

  
    
  
```

```javascript
import { MilvusClient, DataType } from "@zilliz/milvus2-sdk-node";

const address = "http://localhost:19530";
const token = "root:Milvus";
const client = new MilvusClient({address, token});

// 4. Single vector search
var query_vector = [0.3580376395471989, -0.6023495712049978, 0.18414012509913835, -0.26286205330961354, 0.9029438446296592],

res = await client.search({
    collection_name: "quick_setup",
    data: query_vector,
    limit: 3, // The number of results to return
})

console.log(res.results)

// [
//   { score: 0.08821295201778412, id: '551' },
//   { score: 0.0800950899720192, id: '296' },
//   { score: 0.07794742286205292, id: '43' }
// ]

  
    
  
```

```bash
export CLUSTER_ENDPOINT="http://localhost:19530"
export TOKEN="root:Milvus"

curl --request POST \
--url "${CLUSTER_ENDPOINT}/v2/vectordb/entities/search" \
--header "Authorization: Bearer ${TOKEN}" \
--header "Content-Type: application/json" \
-d '{
    "collectionName": "quick_setup",
    "data": [
        [0.3580376395471989, -0.6023495712049978, 0.18414012509913835, -0.26286205330961354, 0.9029438446296592]
    ],
    "annsField": "vector",
    "limit": 3
}'

# {
#     "code": 0,
#     "data": [
#         {
#             "distance": 0.08821295201778412,
#             "id": 551
#         },
#         {
#             "distance": 0.0800950899720192,
#             "id": 296
#         },
#         {
#             "distance": 0.07794742286205292,
#             "id": 43
#         }
#     ]
# }

  
    
  
```

Milvus 根据搜索结果与查询向量的相似度得分从高到低排列搜索结果。相似度得分也称为与查询向量的距离，其值范围随使用的度量类型而变化。

下表列出了适用的度量类型和相应的距离范围。

度量类型

特征

距离范围

`L2`

值越小表示相似度越高。

\[0, ∞)

`IP`

数值越大，表示相似度越高。

\[-1, 1\]

`COSINE`

数值越大，表示相似度越高。

\[-1, 1\]

`JACCARD`

值越小，表示相似度越高。

\[0, 1\]

`HAMMING`

值越小，表示相似度越高。

\[0，dim(向量)\] 批量向量搜索

## 批量向量搜索

同样，您也可以在一个搜索请求中包含多个查询向量。Milvus 将并行对查询向量进行 ANN 搜索，并返回两组结果。

[Python](#python) [Java](#java) [Go](#go) [NodeJS](#javascript) [cURL](#bash)

```python
# 7. Search with multiple vectors
# 7.1. Prepare query vectors
query_vectors = [
    [0.041732933, 0.013779674, -0.027564144, -0.013061441, 0.009748648],
    [0.0039737443, 0.003020432, -0.0006188639, 0.03913546, -0.00089768134]
]

# 7.2. Start search
res = client.search(
    collection_name="quick_setup",
    data=query_vectors,
    limit=3,
)

for hits in res:
    print("TopK results:")
    for hit in hits:
        print(hit)

# Output
#
# [
#     [
#         {
#             "id": 551,
#             "distance": 0.08821295201778412,
#             "entity": {}
#         },
#         {
#             "id": 296,
#             "distance": 0.0800950899720192,
#             "entity": {}
#         },
#         {
#             "id": 43,
#             "distance": 0.07794742286205292,
#             "entity": {}
#         }
#     ],
#     [
#         {
#             "id": 730,
#             "distance": 0.04431751370429993,
#             "entity": {}
#         },
#         {
#             "id": 333,
#             "distance": 0.04231833666563034,
#             "entity": {}
#         },
#         {
#             "id": 232,
#             "distance": 0.04221535101532936,
#             "entity": {}
#         }
#     ]
# ]

  
    
  
```

```java
import io.milvus.v2.service.vector.request.SearchReq
import io.milvus.v2.service.vector.request.data.BaseVector;
import io.milvus.v2.service.vector.request.data.FloatVec;
import io.milvus.v2.service.vector.response.SearchResp

List<BaseVector> queryVectors = Arrays.asList(
        new FloatVec(new float[]{0.041732933f, 0.013779674f, -0.027564144f, -0.013061441f, 0.009748648f}),
        new FloatVec(new float[]{0.0039737443f, 0.003020432f, -0.0006188639f, 0.03913546f, -0.00089768134f})
);
SearchReq searchReq = SearchReq.builder()
        .collectionName("quick_setup")
        .data(queryVectors)
        .topK(3)
        .build();

SearchResp searchResp = client.search(searchReq);

List<List<SearchResp.SearchResult>> searchResults = searchResp.getSearchResults();
for (List<SearchResp.SearchResult> results : searchResults) {
    System.out.println("TopK results:");
    for (SearchResp.SearchResult result : results) {
        System.out.println(result);
    }
}

// Output
// TopK results:
// SearchResp.SearchResult(entity={}, score=0.49548206, id=1)
// SearchResp.SearchResult(entity={}, score=0.320147, id=3)
// SearchResp.SearchResult(entity={}, score=0.107413776, id=6)
// TopK results:
// SearchResp.SearchResult(entity={}, score=0.5678123, id=6)
// SearchResp.SearchResult(entity={}, score=0.32368967, id=2)
// SearchResp.SearchResult(entity={}, score=0.24108477, id=3)

  
    
  
```

```go
queryVectors := []entity.Vector{
    entity.FloatVector([]float32{0.3580376395471989, -0.6023495712049978, 0.18414012509913835, -0.26286205330961354, 0.9029438446296592}),
    entity.FloatVector([]float32{0.19886812562848388, 0.06023560599112088, 0.6976963061752597, 0.2614474506242501, 0.838729485096104}),
}

resultSets, err := client.Search(ctx, milvusclient.NewSearchOption(
    "quick_setup", // collectionName
    3,               // limit
    queryVectors,
).WithConsistencyLevel(entity.ClStrong).
    WithANNSField("vector"))
if err != nil {
    fmt.Println(err.Error())
    // handle error
}

for _, resultSet := range resultSets {
    fmt.Println("IDs: ", resultSet.IDs.FieldData().GetScalars())
    fmt.Println("Scores: ", resultSet.Scores)
}

  
    
  
```

```javascript
// 7. Search with multiple vectors
const query_vectors = [
    [0.3580376395471989, -0.6023495712049978, 0.18414012509913835, -0.26286205330961354, 0.9029438446296592], 
    [0.19886812562848388, 0.06023560599112088, 0.6976963061752597, 0.2614474506242501, 0.838729485096104]
]

res = await client.search({
    collection_name: "quick_setup",
    vectors: query_vectors,
    limit: 3,
})

console.log(res.results)

// Output
// 
// [
//   [
//     { score: 0.08821295201778412, id: '551' },
//     { score: 0.0800950899720192, id: '296' },
//     { score: 0.07794742286205292, id: '43' }
//   ],
//   [
//     { score: 0.04431751370429993, id: '730' },
//     { score: 0.04231833666563034, id: '333' },
//     { score: 0.04221535101532936, id: '232' },
//   ]
// ]

  
    
  
```

```bash
export CLUSTER_ENDPOINT="http://localhost:19530"
export TOKEN="root:Milvus"

curl --request POST \
--url "${CLUSTER_ENDPOINT}/v2/vectordb/entities/search" \
--header "Authorization: Bearer ${TOKEN}" \
--header "Content-Type: application/json" \
-d '{
    "collectionName": "quick_setup",
    "data": [
        [0.3580376395471989, -0.6023495712049978, 0.18414012509913835, -0.26286205330961354, 0.9029438446296592],
        [0.19886812562848388, 0.06023560599112088, 0.6976963061752597, 0.2614474506242501, 0.838729485096104]
    ],
    "annsField": "vector",
    "limit": 3
}'

# {
#     "code": 0,
#     "data": [
#         [
#           {
#               "distance": 0.08821295201778412,
#               "id": 551
#           },
#           {
#               "distance": 0.0800950899720192,
#               "id": 296
#           },
#           {
#               "distance": 0.07794742286205292,
#               "id": 43
#           }
#         ],
#         [
#           {
#               "distance": 0.04431751370429993,
#               "id": 730
#           },
#           {
#               "distance": 0.04231833666563034,
#               "id": 333
#           },
#           {
#               "distance": 0.04221535101532936,
#               "id": 232
#           }
#        ]
#     ],
#     "topks":[3]
# }

  
    
  
```

## 主键搜索Compatible with Milvus 2.6.9+

如果目标 Collections 中已经存在查询向量，则可以使用主键来代替设置查询向量。

[Python](#python) [Java](#java) [NodeJS](#javascript) [Go](#go) [cURL](#bash)

```python
res = client.search(
    collection_name="quick_setup",
    anns_field="vector",
    ids=[551, 296, 43],
    limit=3,
    search_params={"metric_type": "IP"}
)

for hits in res:
    for hit in hits:
        print(hit)

  
    
  
```

```java
// java

  
    
  
```

```javascript
// node.js

  
    
  
```

```go
// go

  
    
  
```

```bash
# restful

  
    
  
```

## 在分区中进行 ANN 搜索

假设您在一个 Collections 中创建了多个分区，您可以将搜索范围缩小到特定数量的分区。在这种情况下，您可以在搜索请求中包含目标分区名称，将搜索范围限制在指定的分区内。减少搜索所涉及的分区数量可以提高搜索性能。

下面的代码片段假定 Collections 中有一个名为**PartitionA**的分区。

[Python](#python) [Java](#java) [Go](#go) [NodeJS](#javascript) [cURL](#bash)

```python
# 4. Single vector search
query_vector = [0.3580376395471989, -0.6023495712049978, 0.18414012509913835, -0.26286205330961354, 0.9029438446296592]
res = client.search(
    collection_name="quick_setup",
    partition_names=["partitionA"],
    data=[query_vector],
    limit=3,
)

for hits in res:
    print("TopK results:")
    for hit in hits:
        print(hit)

# [
#     [
#         {
#             "id": 551,
#             "distance": 0.08821295201778412,
#             "entity": {}
#         },
#         {
#             "id": 296,
#             "distance": 0.0800950899720192,
#             "entity": {}
#         },
#         {
#             "id": 43,
#             "distance": 0.07794742286205292,
#             "entity": {}
#         }
#     ]
# ]

  
    
  
```

```java
import io.milvus.v2.service.vector.request.SearchReq
import io.milvus.v2.service.vector.request.data.FloatVec;
import io.milvus.v2.service.vector.response.SearchResp

FloatVec queryVector = new FloatVec(new float[]{0.3580376395471989f, -0.6023495712049978f, 0.18414012509913835f, -0.26286205330961354f, 0.9029438446296592f});
SearchReq searchReq = SearchReq.builder()
        .collectionName("quick_setup")
        .partitionNames(Collections.singletonList("partitionA"))
        .data(Collections.singletonList(queryVector))
        .topK(3)
        .build();

SearchResp searchResp = client.search(searchReq);

List<List<SearchResp.SearchResult>> searchResults = searchResp.getSearchResults();
for (List<SearchResp.SearchResult> results : searchResults) {
    System.out.println("TopK results:");
    for (SearchResp.SearchResult result : results) {
        System.out.println(result);
    }
}

// Output
// TopK results:
// SearchResp.SearchResult(entity={}, score=0.6395302, id=13)
// SearchResp.SearchResult(entity={}, score=0.5408028, id=12)
// SearchResp.SearchResult(entity={}, score=0.49696884, id=17)

  
    
  
```

```go
queryVector := []float32{0.3580376395471989, -0.6023495712049978, 0.18414012509913835, -0.26286205330961354, 0.9029438446296592}

resultSets, err := client.Search(ctx, milvusclient.NewSearchOption(
    "quick_setup", // collectionName
    3,               // limit
    []entity.Vector{entity.FloatVector(queryVector)},
).WithConsistencyLevel(entity.ClStrong).
    WithPartitions("partitionA").
    WithANNSField("vector"))
if err != nil {
    fmt.Println(err.Error())
    // handle error
}

for _, resultSet := range resultSets {
    fmt.Println("IDs: ", resultSet.IDs.FieldData().GetScalars())
    fmt.Println("Scores: ", resultSet.Scores)
}

  
    
  
```

```javascript
// 4. Single vector search
var query_vector = [0.3580376395471989, -0.6023495712049978, 0.18414012509913835, -0.26286205330961354, 0.9029438446296592],

res = await client.search({
    collection_name: "quick_setup",
    partition_names: ["partitionA"],
    data: query_vector,
    limit: 3, // The number of results to return
})

console.log(res.results)

// [
//   { score: 0.08821295201778412, id: '551' },
//   { score: 0.0800950899720192, id: '296' },
//   { score: 0.07794742286205292, id: '43' }
// ]

  
    
  
```

```bash
export CLUSTER_ENDPOINT="http://localhost:19530"
export TOKEN="root:Milvus"

curl --request POST \
--url "${CLUSTER_ENDPOINT}/v2/vectordb/entities/search" \
--header "Authorization: Bearer ${TOKEN}" \
--header "Content-Type: application/json" \
-d '{
    "collectionName": "quick_setup",
    "partitionNames": ["partitionA"],
    "data": [
        [0.3580376395471989, -0.6023495712049978, 0.18414012509913835, -0.26286205330961354, 0.9029438446296592]
    ],
    "annsField": "vector",
    "limit": 3
}'

# {
#     "code": 0,
#     "data": [
#         {
#             "distance": 0.08821295201778412,
#             "id": 551
#         },
#         {
#             "distance": 0.0800950899720192,
#             "id": 296
#         },
#         {
#             "distance": 0.07794742286205292,
#             "id": 43
#         }
#     ],
#     "topks":[3]
# }

  
    
  
```

## 使用输出字段

在搜索结果中，Milvus 默认会包含包含 TOP-K 向量嵌入的实体的主字段值和相似度距离/分数。您可以在搜索请求中包含目标字段（包括向量和标量字段）的名称作为输出字段，以使搜索结果携带这些实体中其他字段的值。

[Python](#python) [Java](#java) [Go](#go) [NodeJS](#javascript) [cURL](#bash)

```python
# 4. Single vector search
query_vector = [0.3580376395471989, -0.6023495712049978, 0.18414012509913835, -0.26286205330961354, 0.9029438446296592],

res = client.search(
    collection_name="quick_setup",
    data=[query_vector],
    limit=3, # The number of results to return
    search_params={"metric_type": "IP"}，
    output_fields=["color"]
)

print(res)

# [
#     [
#         {
#             "id": 551,
#             "distance": 0.08821295201778412,
#             "entity": {
#                 "color": "orange_6781"
#             }
#         },
#         {
#             "id": 296,
#             "distance": 0.0800950899720192,
#             "entity": {
#                 "color": "red_4794"
#             }
#         },
#         {
#             "id": 43,
#             "distance": 0.07794742286205292,
#             "entity": {
#                 "color": "grey_8510"
#             }
#         }
#     ]
# ]

  
    
  
```

```java
import io.milvus.v2.service.vector.request.SearchReq
import io.milvus.v2.service.vector.request.data.FloatVec;
import io.milvus.v2.service.vector.response.SearchResp

FloatVec queryVector = new FloatVec(new float[]{0.3580376395471989f, -0.6023495712049978f, 0.18414012509913835f, -0.26286205330961354f, 0.9029438446296592f});
SearchReq searchReq = SearchReq.builder()
        .collectionName("quick_setup")
        .data(Collections.singletonList(queryVector))
        .topK(3)
        .outputFields(Collections.singletonList("color"))
        .build();

SearchResp searchResp = client.search(searchReq);

List<List<SearchResp.SearchResult>> searchResults = searchResp.getSearchResults();
for (List<SearchResp.SearchResult> results : searchResults) {
    System.out.println("TopK results:");
    for (SearchResp.SearchResult result : results) {
        System.out.println(result);
    }
}

// Output
// TopK results:
// SearchResp.SearchResult(entity={color=black_9955}, score=0.95944905, id=5)
// SearchResp.SearchResult(entity={color=red_7319}, score=0.8689616, id=1)
// SearchResp.SearchResult(entity={color=white_5015}, score=0.866088, id=7)

  
    
  
```

```go
queryVector := []float32{0.3580376395471989, -0.6023495712049978, 0.18414012509913835, -0.26286205330961354, 0.9029438446296592}

resultSets, err := client.Search(ctx, milvusclient.NewSearchOption(
    "quick_setup", // collectionName
    3,               // limit
    []entity.Vector{entity.FloatVector(queryVector)},
).WithConsistencyLevel(entity.ClStrong).
    WithANNSField("vector").
    WithOutputFields("color"))
if err != nil {
    fmt.Println(err.Error())
    // handle error
}

for _, resultSet := range resultSets {
    fmt.Println("IDs: ", resultSet.IDs.FieldData().GetScalars())
    fmt.Println("Scores: ", resultSet.Scores)
    fmt.Println("color: ", resultSet.GetColumn("color").FieldData().GetScalars())
}

  
    
  
```

```javascript
// 4. Single vector search
var query_vector = [0.3580376395471989, -0.6023495712049978, 0.18414012509913835, -0.26286205330961354, 0.9029438446296592],

res = await client.search({
    collection_name: "quick_setup",
    data: query_vector,
    limit: 3, // The number of results to return
    output_fields: ["color"]
})

console.log(res.results)

// [
//   { score: 0.08821295201778412, id: '551', entity: {"color": "orange_6781"}},
//   { score: 0.0800950899720192, id: '296' entity: {"color": "red_4794"}},
//   { score: 0.07794742286205292, id: '43' entity: {"color": "grey_8510"}}
// ]

  
    
  
```

```bash
export CLUSTER_ENDPOINT="http://localhost:19530"
export TOKEN="root:Milvus"

curl --request POST \
--url "${CLUSTER_ENDPOINT}/v2/vectordb/entities/search" \
--header "Authorization: Bearer ${TOKEN}" \
--header "Content-Type: application/json" \
-d '{
    "collectionName": "quick_setup",
    "data": [
        [0.3580376395471989, -0.6023495712049978, 0.18414012509913835, -0.26286205330961354, 0.9029438446296592]
    ],
    "annsField": "vector",
    "limit": 3,
    "outputFields": ["color"]
}'

# {
#     "code": 0,
#     "data": [
#         {
#             "distance": 0.08821295201778412,
#             "id": 551,
#             "color": "orange_6781"
#         },
#         {
#             "distance": 0.0800950899720192,
#             "id": 296,
#             "color": "red_4794"
#         },
#         {
#             "distance": 0.07794742286205292,
#             "id": 43
#             "color": "grey_8510"
#         }
#     ],
#     "topks":[3]
# }

  
    
  
```

## 使用限制和偏移

您可能会注意到，搜索请求中包含的参数`limit` 决定了搜索结果中包含的实体数量。该参数指定了单次搜索中返回实体的最大数量，通常称为**top-K。**

如果希望执行分页查询，可以使用循环来发送多个搜索请求，每个查询请求中都包含**Limit**和**Offset**参数。具体来说，可以将 "**限制 "**参数设置为希望包含在当前查询结果中的实体数量，将 "**偏移**"参数设置为已经返回的实体总数。

下表概述了在一次返回 100 个 "实体 "时，如何为分页查询设置 "**限制**"和**"偏移**"参数。

查询

每次查询要返回的实体

已返回实体总数

**第 1 次**查询

100

0

**第二次**查询

100

100

**第三次**查询

100

200

**第 n 次**查询

100

100 x (n-1)

请注意，在一次 ANN 搜索中，`limit` 和`offset` 的总和应小于 16 384。

[Python](#python) [Java](#java) [Go](#go) [NodeJS](#javascript) [cURL](#bash)

```python
# 4. Single vector search
query_vector = [0.3580376395471989, -0.6023495712049978, 0.18414012509913835, -0.26286205330961354, 0.9029438446296592],

res = client.search(
    collection_name="quick_setup",
    data=[query_vector],
    limit=3, # The number of results to return
    search_params={
        "metric_type": "IP", 
        "offset": 10 # The records to skip
    }
)

  
    
  
```

```java
import io.milvus.v2.service.vector.request.SearchReq
import io.milvus.v2.service.vector.request.data.FloatVec;
import io.milvus.v2.service.vector.response.SearchResp

FloatVec queryVector = new FloatVec(new float[]{0.3580376395471989f, -0.6023495712049978f, 0.18414012509913835f, -0.26286205330961354f, 0.9029438446296592f});
SearchReq searchReq = SearchReq.builder()
        .collectionName("quick_setup")
        .data(Collections.singletonList(queryVector))
        .topK(3)
        .offset(10)
        .build();

SearchResp searchResp = client.search(searchReq);

List<List<SearchResp.SearchResult>> searchResults = searchResp.getSearchResults();
for (List<SearchResp.SearchResult> results : searchResults) {
    System.out.println("TopK results:");
    for (SearchResp.SearchResult result : results) {
        System.out.println(result);
    }
}

// Output
// TopK results:
// SearchResp.SearchResult(entity={}, score=0.24120237, id=16)
// SearchResp.SearchResult(entity={}, score=0.22559784, id=9)
// SearchResp.SearchResult(entity={}, score=-0.09906838, id=2)

  
    
  
```

```go
queryVector := []float32{0.3580376395471989, -0.6023495712049978, 0.18414012509913835, -0.26286205330961354, 0.9029438446296592}

resultSets, err := client.Search(ctx, milvusclient.NewSearchOption(
    "quick_setup", // collectionName
    3,               // limit
    []entity.Vector{entity.FloatVector(queryVector)},
).WithConsistencyLevel(entity.ClStrong).
    WithANNSField("vector").
    WithOffset(10))
if err != nil {
    fmt.Println(err.Error())
    // handle error
}

for _, resultSet := range resultSets {
    fmt.Println("IDs: ", resultSet.IDs.FieldData().GetScalars())
    fmt.Println("Scores: ", resultSet.Scores)
}

  
    
  
```

```javascript
// 4. Single vector search
var query_vector = [0.3580376395471989, -0.6023495712049978, 0.18414012509913835, -0.26286205330961354, 0.9029438446296592],

res = await client.search({
    collection_name: "quick_setup",
    data: query_vector,
    limit: 3, // The number of results to return,
    offset: 10 // The record to skip.
})

  
    
  
```

```bash
export CLUSTER_ENDPOINT="http://localhost:19530"
export TOKEN="root:Milvus"

curl --request POST \
--url "${CLUSTER_ENDPOINT}/v2/vectordb/entities/search" \
--header "Authorization: Bearer ${TOKEN}" \
--header "Content-Type: application/json" \
-d '{
    "collectionName": "quick_setup",
    "data": [
        [0.3580376395471989, -0.6023495712049978, 0.18414012509913835, -0.26286205330961354, 0.9029438446296592]
    ],
    "annsField": "vector",
    "limit": 3,
    "offset": 10
}'

  
    
  
```

## 为搜索临时设置时区

如果您的 Collections 有`TIMESTAMPTZ` 字段，您可以通过在搜索调用中设置`timezone` 参数，为单次操作临时覆盖数据库或 Collections 的默认时区。这可以控制`TIMESTAMPTZ` 值在操作过程中的显示和比较方式。

`timezone` 的值必须是有效的[IANA 时区标识符](https://en.wikipedia.org/wiki/List_of_tz_database_time_zones)（例如，**亚洲/上海**、**美国/芝加哥**或**UTC**）。有关如何使用`TIMESTAMPTZ` 字段的详细信息，请参阅[TIMESTAMPTZ 字段](/docs/zh/timestamptz-field.md)。

下面的示例展示了如何为搜索操作临时设置时区：

[Python](#python) [Java](#java) [NodeJS](#javascript) [Go](#go) [cURL](#bash)

```python
res = client.search(
    collection_name="quick_setup",
    anns_field="vector",
    data=[query_vector],
    limit=3,
    search_params={"metric_type": "IP"},
    timezone="America/Havana",
)

  
    
  
```

```java
// java

  
    
  
```

```javascript
// js

  
    
  
```

```go
// go

  
    
  
```

```bash
# restful

  
    
  
```

## 增强 ANN 搜索

AUTOINDEX 可大大拉平 ANN 搜索的学习曲线。然而，随着 Top-K 的增加，搜索结果不一定总是正确的。通过缩小搜索范围、提高搜索结果相关性和搜索结果多样化，Milvus 实现了以下搜索增强功能。

-   过滤搜索

    您可以在搜索请求中包含过滤条件，这样 Milvus 就会在进行 ANN 搜索前进行元数据过滤，将搜索范围从整个 Collections 缩小到只搜索符合指定过滤条件的实体。

    有关元数据过滤和过滤条件的更多信息，请参阅[过滤搜索](/docs/zh/filtered-search.md)、[过滤解释](/docs/zh/boolean.md)和相关主题。

-   范围搜索

    您可以在特定范围内限制返回实体的距离或得分，从而提高搜索结果的相关性。在 Milvus 中，范围搜索包括以与查询向量最相似的嵌入向量为中心，画两个同心圆。搜索请求指定了两个圆的半径，Milvus 会返回所有属于外圆但不属于内圆的向量嵌入。

    有关范围搜索的更多信息，请参阅[范围搜索](/docs/zh/range-search.md)。

-   分组搜索

    如果返回的实体在特定字段中持有相同的值，搜索结果可能无法代表向量空间中所有向量嵌入的分布情况。要使搜索结果多样化，可以考虑使用分组搜索。

    有关分组搜索的更多信息，请参阅[分组搜索](/docs/zh/grouping-search.md)、

-   混合搜索

    一个 Collections 可以包含多个向量场，以保存使用不同嵌入模型生成的向量嵌入。通过这种方式，可以使用混合搜索对这些向量场的搜索结果进行 Rerankers，从而提高召回率。

    有关混合搜索的更多信息，请参阅[混合搜索](/docs/zh/multi-vector-search.md)。

-   搜索迭代器

    单个 ANN 搜索最多可返回 16,384 个实体。如果需要在单次搜索中返回更多实体，请考虑使用搜索迭代器。

    有关搜索迭代器的详细信息，请参阅搜索[迭代器](/docs/zh/with-iterators.md)。

-   全文搜索

    全文搜索是一种在文本数据集中检索包含特定术语或短语的文档，然后根据相关性对结果进行排序的功能。该功能克服了语义搜索的局限性（语义搜索可能会忽略精确的术语），确保您获得最准确且与上下文最相关的结果。此外，它还能接受原始文本输入，自动将文本数据转换为稀疏嵌入，无需手动生成向量嵌入，从而简化了向量搜索。

    有关全文搜索的详细信息，请参阅[全文搜索](/docs/zh/full-text-search.md)。

-   文本匹配

    Milvus 中的关键词匹配功能可根据特定术语精确检索文档。该功能主要用于满足特定条件的过滤搜索，并可结合标量过滤来完善查询结果，允许在符合标量标准的向量内进行相似性搜索。

    有关关键字匹配的详细信息，请参阅[关键字匹配](/docs/zh/keyword-match.md)。

-   使用 Partition Key

    在元数据过滤中涉及多个标量字段并使用相当复杂的过滤条件可能会影响搜索效率。一旦将一个标量字段设置为分区关键字，并在搜索请求中使用涉及分区关键字的过滤条件，就可以帮助将搜索范围限制在与指定分区关键字值相对应的分区内。

    有关分区键的详细信息，请参阅[使用分区键](/docs/zh/use-partition-key.md)。

-   使用 mmap

    有关 mmap 设置的详情，请参阅[使用 mmap](/docs/zh/mmap.md)。

-   聚类压缩

    有关聚类压缩的详情，请参阅[聚类](/docs/zh/clustering-compaction.md)压缩。

-   使用 Reranking

    有关使用排名器增强搜索结果相关性的详情，请参阅[衰减排名器概述](/docs/zh/decay-ranker-overview.md)和[模型排名器概述](/docs/zh/model-ranker-overview.md)。


翻译自![DeepL](/_next/static/media/deepl.d32c6a10.png)

##### 目录

-   [基本向量搜索](#Basic-Vector-Search)
-   [概述](#Overview)
-   [单向量搜索](#Single-Vector-Search)
-   [批量向量搜索](#Bulk-Vector-Search)
-   [主键搜索Compatible with Milvus 2.6.9+](#Primary-Key-Search--Milvus-269+)
-   [在分区中进行 ANN 搜索](#ANN-Search-in-Partition)
-   [使用输出字段](#Use-Output-Fields)
-   [使用限制和偏移](#Use-Limit-and-Offset)
-   [为搜索临时设置时区](#Temporarily-set-a-timezone-for-a-search)
-   [增强 ANN 搜索](#Enhancing-ANN-Search)

## 想要更快、更简单、更好用的 Milvus SaaS服务 ？

Zilliz Cloud是基于Milvus的全托管向量数据库，拥有更高性能，更易扩展，以及卓越性价比

[免费试用 Zilliz Cloud](https://cloud.zilliz.com.cn/signup?utm_source=milvusio&utm_medium=referral&utm_campaign=milvus_right_card&utm_content=docs/zh/single-vector-search.md)

##### 反馈

此页对您是否有帮助?

Ask AI![](/inkeep/milvus-icon-white.png)