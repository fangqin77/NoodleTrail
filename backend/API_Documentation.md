# 陕西美食API接口文档

## 基础信息
- **基础URL**: `http://localhost:8080/api/foods`
- **Content-Type**: `application/json`
- **编码**: UTF-8

## 接口列表

### 1. 通用搜索接口
**接口地址**: `GET /api/foods/search`

**功能描述**: 根据美食名称或标签搜索美食信息

**请求参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| name | String | 否 | 美食名称，支持模糊搜索 |
| tag | String | 否 | 美食标签，支持模糊搜索和精确匹配 |

**请求示例**:
```bash
# 按名称搜索
GET /api/foods/search?name=肉夹馍

# 按标签搜索
GET /api/foods/search?tag=小吃

# 组合搜索
GET /api/foods/search?name=馍&tag=小吃

# 查询所有（不传参数）
GET /api/foods/search
```

**响应格式**:
```json
[
  {
    "id": 1,
    "foodName": "肉夹馍",
    "imageUrl": "http://example.com/roujiamo.jpg",
    "history": "肉夹馍起源于唐代...",
    "introduction": "陕西著名传统小吃...",
    "features": "外酥里嫩，肉香四溢",
    "tag": "小吃,面食,肉类"
  }
]
```

注意：现在API返回的是标准的JSON对象数组，字段名与Java实体类一致。

### 2. 按名称搜索接口
**接口地址**: `GET /api/foods/search/by-name`

**功能描述**: 专门用于按美食名称搜索

**请求参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| name | String | 是 | 美食名称，支持模糊搜索 |

**请求示例**:
```bash
GET /api/foods/search/by-name?name=凉皮
```

### 3. 按标签搜索接口
**接口地址**: `GET /api/foods/search/by-tag`

**功能描述**: 专门用于按标签搜索

**请求参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| tag | String | 是 | 美食标签，支持模糊搜索和精确匹配 |

**请求示例**:
```bash
GET /api/foods/search/by-tag?tag=主食
```

## 返回数据字段说明

| 字段名 | 类型 | 说明 |
|--------|------|------|
| id | Integer | 美食ID |
| foodName | String | 美食名称 |
| imageUrl | String | 图片URL |
| history | String | 历史背景 |
| introduction | String | 美食介绍 |
| features | String | 特色描述 |
| tag | String | 美食标签（多个标签用逗号分隔） |

## 错误响应

当请求出错时，会返回相应的HTTP状态码和错误信息：

| 状态码 | 说明 |
|--------|------|
| 200 | 请求成功 |
| 400 | 请求参数错误 |
| 500 | 服务器内部错误 |

## 使用建议

1. **模糊搜索**: 接口支持模糊匹配，可以使用部分关键词进行搜索
2. **标签搜索**: 支持多种标签格式，可以用逗号分隔的多标签进行搜索
3. **性能优化**: 建议在前端实现防抖功能，避免频繁调用接口
4. **分页**: 当前接口返回所有匹配结果，如需分页可在后续版本中添加

## 前端调用示例

### JavaScript (Axios)
```javascript
// 搜索美食
async function searchFoods(name, tag) {
  try {
    const response = await axios.get('/api/foods/search', {
      params: {
        name: name || undefined,
        tag: tag || undefined
      }
    });
    return response.data;
  } catch (error) {
    console.error('搜索失败:', error);
    throw error;
  }
}

// 使用示例
searchFoods('肉夹馍', null).then(data => {
  console.log('搜索结果:', data);
});
```

### jQuery
```javascript
function searchFoods(name, tag) {
  return $.ajax({
    url: '/api/foods/search',
    method: 'GET',
    data: {
      name: name,
      tag: tag
    },
    dataType: 'json'
  });
}

// 使用示例
searchFoods('', '小吃').done(function(data) {
  console.log('小吃类美食:', data);
});
```

### 原生JavaScript
```javascript
async function searchFoods(name, tag) {
  const params = new URLSearchParams();
  if (name) params.append('name', name);
  if (tag) params.append('tag', tag);
  
  const response = await fetch(`/api/foods/search?${params}`);
  if (!response.ok) {
    throw new Error('搜索失败');
  }
  return await response.json();
}
```