# AI Embedding Service

Service để generate embeddings cho semantic search trong hệ thống quản lý sáng kiến.

## Chức năng

- Generate embedding cho text đơn lẻ
- Generate embeddings batch cho nhiều texts
- Health check endpoint

## Model

- **Model**: `paraphrase-multilingual-MiniLM-L12-v2`
- **Dimension**: 384
- **Language**: Multilingual (hỗ trợ tiếng Việt)

## API Endpoints

### GET /health
Health check endpoint

### POST /embed
Generate embedding cho một text

**Request:**
```json
{
  "text": "Tên sáng kiến và nội dung..."
}
```

**Response:**
```json
{
  "embedding": [0.1, 0.2, ...],
  "dimension": 384
}
```

### POST /embed/batch
Generate embeddings cho nhiều texts

**Request:**
```json
{
  "texts": ["Text 1", "Text 2", ...]
}
```

**Response:**
```json
{
  "embeddings": [[0.1, 0.2, ...], [0.3, 0.4, ...]],
  "count": 2,
  "dimension": 384
}
```

## Cách chạy

### Chạy Local (Development)

#### Bước 1: Tạo Virtual Environment

```bash
# Windows
python -m venv venv
venv\Scripts\activate

# Linux/Mac
python3 -m venv venv
source venv/bin/activate
```

#### Bước 2: Cài đặt Dependencies

```bash
pip install -r requirements.txt
```

#### Bước 3: Chạy Service

```bash
# Cách 1: Chạy trực tiếp
python main.py

# Cách 2: Dùng uvicorn (khuyến nghị)
uvicorn main:app --host 0.0.0.0 --port 8000 --reload
```

Service sẽ chạy tại: `http://localhost:8000`

**Lưu ý:**
- Lần đầu chạy sẽ download model (~420MB), mất khoảng 2-3 phút
- Model được cache tại `~/.cache/huggingface/` để không download lại
- Flag `--reload` cho phép auto-reload khi code thay đổi (chỉ dùng khi development)

#### Bước 4: Test Service

```bash
# Test health check
curl http://localhost:8000/health

# Test generate embedding
curl -X POST http://localhost:8000/embed \
  -H "Content-Type: application/json" \
  -d '{"text": "Sáng kiến test embedding"}'
```

### Chạy qua Docker (Production)

```bash
# Build image
docker build -t innovation-ai-embedding-service .

# Run container
docker run -p 8000:8000 innovation-ai-embedding-service
```

Hoặc dùng Docker Compose (từ thư mục root của BE):

```bash
docker compose up -d ai-embedding-service
```

## Lưu ý

- **Model download**: Model sẽ được download lần đầu khi start (khoảng 420MB)
- **Model cache**: 
  - Local: Model được cache tại `~/.cache/huggingface/`
  - Docker: Model được cache trong volume `ai-model-cache`
- **Start time**: Service cần ~2-3 phút để start lần đầu (download model)
- **Port**: Mặc định chạy trên port 8000
- **Environment**: Khi chạy local, đảm bảo set `AI_SERVICE_URL=http://localhost:8000` cho BE

