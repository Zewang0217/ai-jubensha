# 执行搜证操作

## OpenAPI Specification

```yaml
openapi: 3.0.1
info:
  title: ''
  version: 1.0.0
paths:
  /api/games/{gameId}/investigation:
    post:
      summary: 执行搜证操作
      deprecated: false
      description: 玩家消耗一次搜证机会，获得指定场景中的一个线索
      tags:
        - 搜证控制器
      parameters:
        - name: gameId
          in: path
          description: 游戏ID
          required: true
          schema:
            type: integer
      requestBody:
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/InvestigationRequestDTO'
              description: 搜证请求（包含玩家ID和场景ID）
            example:
              playerId: 1
              sceneId: 2
              clueId: 3
              remark: xxx
      responses:
        '200':
          description: ''
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/ResponseEntity%3F'
                description: 搜证响应，包含获得的线索和剩余次数
          headers: { }
          x-apifox-ordering: 0
      security: [ ]
      x-apifox-folder: 搜证控制器
      x-apifox-status: released
      x-run-in-apifox: https://app.apifox.com/web/project/7788732/apis/api-417553769-run
components:
  schemas:
    InvestigationRequestDTO:
      type: object
      properties:
        playerId:
          type: integer
          description: 玩家ID
          format: int64
        sceneId:
          type: integer
          description: 场景ID
          format: int64
        clueId:
          type: integer
          description: 可选：指定线索ID（如果不指定则随机返回场景中的一个线索）
          format: int64
        remark:
          type: string
          description: 可选：搜证备注或玩家留言
      x-apifox-orders:
        - playerId
        - sceneId
        - clueId
        - remark
      required:
        - playerId
        - sceneId
      x-apifox-ignore-properties: [ ]
      x-apifox-folder: ''
    ResponseEntity?:
      type: object
      properties: { }
      x-apifox-orders: [ ]
      x-apifox-ignore-properties: [ ]
      x-apifox-folder: ''
  responses: { }
  securitySchemes: { }
servers:
  - url: http://localhost:8088
    description: 开发环境
security: [ ]

```

## example response

```json

```

# 获取玩家的搜证状态

## OpenAPI Specification

```yaml
openapi: 3.0.1
info:
  title: ''
  version: 1.0.0
paths:
  /api/games/{gameId}/investigation/status:
    get:
      summary: 获取玩家的搜证状态
      deprecated: false
      description: 包括剩余次数、搜证历史等信息
      tags:
        - 搜证控制器
      parameters:
        - name: gameId
          in: path
          description: 游戏ID
          required: true
          schema:
            type: integer
        - name: playerId
          in: query
          description: 玩家ID
          required: true
          schema:
            type: integer
      responses:
        '200':
          description: ''
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/ResponseEntity%3F'
                description: 搜证状态
          headers: { }
          x-apifox-ordering: 0
      security: [ ]
      x-apifox-folder: 搜证控制器
      x-apifox-status: released
      x-run-in-apifox: https://app.apifox.com/web/project/7788732/apis/api-417553770-run
components:
  schemas:
    ResponseEntity?:
      type: object
      properties: { }
      x-apifox-orders: [ ]
      x-apifox-ignore-properties: [ ]
      x-apifox-folder: ''
  responses: { }
  securitySchemes: { }
servers:
  - url: http://localhost:8088
    description: 开发环境
security: [ ]

```