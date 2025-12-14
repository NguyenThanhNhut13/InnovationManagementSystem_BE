from fastapi import FastAPI, HTTPException
from pydantic import BaseModel
from sentence_transformers import SentenceTransformer
import uvicorn
import logging

# Setup logging
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(name)s - %(levelname)s - %(message)s'
)
logger = logging.getLogger(__name__)

app = FastAPI(
    title="AI Embedding Service",
    description="Service để generate embeddings cho semantic search",
    version="1.0.0"
)

# Load model (sẽ download lần đầu nếu chưa có)
logger.info("Đang load model paraphrase-multilingual-MiniLM-L12-v2...")
try:
    model = SentenceTransformer('paraphrase-multilingual-MiniLM-L12-v2')
    logger.info("Model loaded successfully!")
except Exception as e:
    logger.error(f"Lỗi khi load model: {e}")
    raise


class EmbedRequest(BaseModel):
    text: str


class EmbedBatchRequest(BaseModel):
    texts: list[str]


class HealthResponse(BaseModel):
    status: str
    model_loaded: bool


@app.get("/health", response_model=HealthResponse)
async def health():
    """Health check endpoint"""
    return HealthResponse(
        status="ok",
        model_loaded=model is not None
    )


@app.post("/embed")
async def get_embedding(request: EmbedRequest):
    """
    Generate embedding cho một text
    
    Args:
        request: EmbedRequest chứa text cần embed
        
    Returns:
        dict với key "embedding" chứa list các float values
    """
    try:
        if not request.text or not request.text.strip():
            raise HTTPException(status_code=400, detail="Text không được để trống")
        
        logger.info(f"Generating embedding cho text (length: {len(request.text)})")
        
        # Generate embedding
        embedding = model.encode(
            request.text,
            convert_to_numpy=True,
            normalize_embeddings=True  # Normalize để cosine similarity hoạt động tốt hơn
        )
        
        # Convert numpy array to list
        embedding_list = embedding.tolist()
        
        logger.info(f"Embedding generated successfully (dimension: {len(embedding_list)})")
        
        return {
            "embedding": embedding_list,
            "dimension": len(embedding_list)
        }
    except Exception as e:
        logger.error(f"Lỗi khi generate embedding: {e}", exc_info=True)
        raise HTTPException(status_code=500, detail=f"Lỗi khi generate embedding: {str(e)}")


@app.post("/embed/batch")
async def get_embeddings_batch(request: EmbedBatchRequest):
    """
    Generate embeddings cho nhiều texts (batch processing)
    
    Args:
        request: EmbedBatchRequest chứa list texts
        
    Returns:
        dict với key "embeddings" chứa list các embeddings
    """
    try:
        if not request.texts or len(request.texts) == 0:
            raise HTTPException(status_code=400, detail="Texts không được để trống")
        
        logger.info(f"Generating embeddings cho {len(request.texts)} texts")
        
        # Generate embeddings batch
        embeddings = model.encode(
            request.texts,
            convert_to_numpy=True,
            normalize_embeddings=True,
            batch_size=32,  # Process 32 texts at a time
            show_progress_bar=False
        )
        
        # Convert to list of lists
        embeddings_list = [emb.tolist() for emb in embeddings]
        
        logger.info(f"Batch embeddings generated successfully")
        
        return {
            "embeddings": embeddings_list,
            "count": len(embeddings_list),
            "dimension": len(embeddings_list[0]) if embeddings_list else 0
        }
    except Exception as e:
        logger.error(f"Lỗi khi generate batch embeddings: {e}", exc_info=True)
        raise HTTPException(status_code=500, detail=f"Lỗi khi generate batch embeddings: {str(e)}")


if __name__ == "__main__":
    uvicorn.run(
        app,
        host="0.0.0.0",
        port=8000,
        log_level="info"
    )

