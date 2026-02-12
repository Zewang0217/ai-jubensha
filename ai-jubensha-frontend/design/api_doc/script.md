# 查询所有剧本

## OpenAPI Specification

```yaml
openapi: 3.0.1
info:
  title: ''
  version: 1.0.0
paths:
  /api/scripts:
    get:
      summary: 查询所有剧本
      deprecated: false
      description: ''
      tags:
        - 剧本控制器
      parameters: [ ]
      responses:
        '200':
          description: ''
          content:
            application/json:
              schema:
                type: array
                items:
                  $ref: '#/components/schemas/ListScriptResponseDTO'
                description: 剧本列表
          headers: { }
          x-apifox-ordering: 0
      security: [ ]
      x-apifox-folder: 剧本控制器
      x-apifox-status: released
      x-run-in-apifox: https://app.apifox.com/web/project/7788732/apis/api-413566865-run
components:
  schemas:
    ListScriptResponseDTO:
      type: object
      properties:
        id:
          type: integer
          description: ''
          format: int64
        name:
          type: string
          description: ''
        description:
          type: string
          description: ''
        author:
          type: string
          description: ''
        difficulty:
          type: string
          description: ''
          enum:
            - EASY
            - MEDIUM
            - HARD
          x-apifox-enum:
            - value: EASY
              name: EASY
              description: EASY
            - value: MEDIUM
              name: MEDIUM
              description: MEDIUM
            - value: HARD
              name: HARD
              description: HARD
        duration:
          type: integer
          description: ''
        playerCount:
          type: integer
          description: ''
        coverImageUrl:
          type: string
          description: ''
        createTime:
          type: string
          description: ''
          x-apifox-mock: '@datetime'
        updateTime:
          type: string
          description: ''
          x-apifox-mock: '@datetime'
      x-apifox-orders:
        - id
        - name
        - description
        - author
        - difficulty
        - duration
        - playerCount
        - coverImageUrl
        - createTime
        - updateTime
      x-apifox-ignore-properties: [ ]
      x-apifox-folder: ''
  responses: { }
  securitySchemes: { }
servers:
  - url: http://localhost:8088
    description: 开发环境
security: [ ]

```

## Example Response

```json
[
  {
    "id": 1,
    "name": "黄泉客栈",
    "description": "民国二十三年，战乱频仍。江南水乡的偏僻小镇上，有一家百年老店“黄泉客栈”。客栈有个诡异传说：每逢月圆之夜，客栈二楼的天字一号房便会传出女子的哭泣声，次日必有客人离奇死亡。今夜又是月圆，六位各怀秘密的客人因暴雨被困客栈。子时刚过，客栈老板钱掌柜被发现死在自己反锁的房间里，胸口插着一把古旧的青铜钥匙，墙上用血写着“欠债还命”。",
    "author": "AI Generated",
    "difficulty": "MEDIUM",
    "duration": 120,
    "playerCount": 6,
    "coverImageUrl": "https://picsum.photos/200/300",
    "createTime": "2026-02-12T22:04:46.823148",
    "updateTime": "2026-02-12T22:04:46.823148"
  }
]
```
