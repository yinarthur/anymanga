# 📱 برومبت لدمج السيرفر مع تطبيق المانجا

## 🎯 الهدف

دمج تطبيق المانجا مع السيرفر المحلي (<http://localhost:3000>) لجلب البيانات من المواقع المختبرة والتي تعمل بنجاح.

---

## 🌐 المواقع المدعومة

### 1. **Azora Moon** (<https://azoramoon.com>)

- **Source ID**: `2482399499047903203`
- **المحرك**: Madara
- **الحالة**: ✅ يعمل بشكل كامل

### 2. **Olympus Staff / Team-X** (<https://olympustaff.com>)

- **Source ID**: `4110737012647903203`
- **المحرك**: Custom
- **الحالة**: ✅ يعمل بشكل جيد

---

## 🔧 خطوات الدمج

### الخطوة 1: إعداد الاتصال بالسيرفر

```kotlin
// في ملف Constants أو Config
object ApiConfig {
    const val BASE_URL = "http://localhost:3000/api/"
    
    // المواقع المدعومة
    val SUPPORTED_SOURCES = listOf(
        MangaSource(
            id = "2482399499047903203",
            name = "Azora Moon",
            baseUrl = "https://azoramoon.com",
            language = "ar"
        ),
        MangaSource(
            id = "4110737012647435874",
            name = "Team-X",
            baseUrl = "https://olympustaff.com",
            language = "ar"
        )
    )
}

data class MangaSource(
    val id: String,
    val name: String,
    val baseUrl: String,
    val language: String
)
```

---

### الخطوة 2: إنشاء API Service

```kotlin
interface MangaApiService {
    
    // البحث عن مصدر بالـ URL
    @POST("search/url")
    suspend fun searchByUrl(@Body request: UrlSearchRequest): SourceResponse
    
    // البحث عن مصدر بالاسم
    @POST("search/name")
    suspend fun searchByName(@Body request: NameSearchRequest): SourceListResponse
    
    // الحصول على API المصدر
    @GET("source/{sourceId}/api")
    suspend fun getSourceApi(@Path("sourceId") sourceId: String): SourceApiResponse
    
    // جلب بيانات الفصل (مع الصور)
    @POST("test/fetch")
    suspend fun fetchChapterData(@Body request: FetchRequest): ChapterDataResponse
    
    // الإحصائيات
    @GET("stats")
    suspend fun getStats(): StatsResponse
}

// Request Models
data class UrlSearchRequest(val url: String)
data class NameSearchRequest(val name: String)
data class FetchRequest(val url: String)

// Response Models
data class SourceResponse(
    val found: Boolean,
    val source: Source?,
    val engine: String?,
    val api: ApiEndpoints?
)

data class Source(
    val id: String,
    val name: String,
    val lang: String,
    val baseUrl: String,
    val nsfw: Boolean
)

data class ChapterDataResponse(
    val success: Boolean,
    val url: String,
    val title: String,
    val contentLength: Int,
    val totalImages: Int,
    val chapterImages: List<String>,
    val preview: String
)
```

---

### الخطوة 3: إنشاء Repository

```kotlin
class MangaRepository(private val apiService: MangaApiService) {
    
    // جلب قائمة المانجا من مصدر معين
    suspend fun getMangaList(sourceUrl: String): Result<List<Manga>> {
        return try {
            val response = apiService.fetchChapterData(
                FetchRequest(url = sourceUrl)
            )
            
            if (response.success) {
                // معالجة البيانات وتحويلها لقائمة مانجا
                Result.success(parseMangaList(response))
            } else {
                Result.failure(Exception("Failed to fetch data"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // جلب تفاصيل مانجا معينة
    suspend fun getMangaDetails(mangaUrl: String): Result<MangaDetails> {
        return try {
            val response = apiService.fetchChapterData(
                FetchRequest(url = mangaUrl)
            )
            
            if (response.success) {
                Result.success(parseMangaDetails(response))
            } else {
                Result.failure(Exception("Failed to fetch manga details"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // جلب صفحات الفصل
    suspend fun getChapterPages(chapterUrl: String): Result<List<String>> {
        return try {
            val response = apiService.fetchChapterData(
                FetchRequest(url = chapterUrl)
            )
            
            if (response.success) {
                Result.success(response.chapterImages)
            } else {
                Result.failure(Exception("Failed to fetch chapter pages"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
```

---

### الخطوة 4: إنشاء ViewModel

```kotlin
class MangaViewModel(
    private val repository: MangaRepository
) : ViewModel() {
    
    private val _mangaList = MutableStateFlow<List<Manga>>(emptyList())
    val mangaList: StateFlow<List<Manga>> = _mangaList.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    // جلب المانجا من Azora Moon
    fun loadMangaFromAzora() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            repository.getMangaList("https://azoramoon.com")
                .onSuccess { mangaList ->
                    _mangaList.value = mangaList
                }
                .onFailure { exception ->
                    _error.value = exception.message
                }
            
            _isLoading.value = false
        }
    }
    
    // جلب المانجا من Olympus Staff
    fun loadMangaFromOlympus() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            repository.getMangaList("https://olympustaff.com")
                .onSuccess { mangaList ->
                    _mangaList.value = mangaList
                }
                .onFailure { exception ->
                    _error.value = exception.message
                }
            
            _isLoading.value = false
        }
    }
    
    // جلب صفحات الفصل
    fun loadChapterPages(chapterUrl: String) {
        viewModelScope.launch {
            _isLoading.value = true
            
            repository.getChapterPages(chapterUrl)
                .onSuccess { pages ->
                    // عرض الصفحات في القارئ
                }
                .onFailure { exception ->
                    _error.value = exception.message
                }
            
            _isLoading.value = false
        }
    }
}
```

---

### الخطوة 5: استخدام في UI

```kotlin
@Composable
fun MangaListScreen(
    viewModel: MangaViewModel = hiltViewModel()
) {
    val mangaList by viewModel.mangaList.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    
    LaunchedEffect(Unit) {
        // جلب المانجا من Azora Moon
        viewModel.loadMangaFromAzora()
    }
    
    Column {
        // زر التبديل بين المصادر
        Row {
            Button(onClick = { viewModel.loadMangaFromAzora() }) {
                Text("Azora Moon")
            }
            Button(onClick = { viewModel.loadMangaFromOlympus() }) {
                Text("Team-X")
            }
        }
        
        when {
            isLoading -> {
                CircularProgressIndicator()
            }
            error != null -> {
                Text("خطأ: $error")
            }
            else -> {
                LazyColumn {
                    items(mangaList) { manga ->
                        MangaItem(manga = manga)
                    }
                }
            }
        }
    }
}
```

---

## 📝 أمثلة على الاستخدام

### مثال 1: جلب مانجا "Only for Love" من Azora Moon

```kotlin
// في ViewModel
fun loadOnlyForLove() {
    viewModelScope.launch {
        // جلب تفاصيل المانجا
        val mangaUrl = "https://azoramoon.com/series/only-for-love"
        repository.getMangaDetails(mangaUrl)
            .onSuccess { details ->
                // عرض التفاصيل: 76 فصل
            }
        
        // جلب الفصل الأول
        val chapterUrl = "https://azoramoon.com/series/only-for-love/chapter-1"
        repository.getChapterPages(chapterUrl)
            .onSuccess { pages ->
                // عرض 59 صفحة
            }
    }
}
```

### مثال 2: جلب "God of Martial Arts" من Olympus Staff

```kotlin
fun loadGodOfMartialArts() {
    viewModelScope.launch {
        val mangaUrl = "https://olympustaff.com/series/god-of-martial-arts"
        repository.getMangaDetails(mangaUrl)
            .onSuccess { details ->
                // عرض التفاصيل: 974 فصل
            }
    }
}
```

---

## ⚠️ ملاحظات مهمة

1. **السيرفر المحلي**: تأكد من تشغيل السيرفر على `http://localhost:3000`
2. **الأذونات**: أضف إذن الإنترنت في `AndroidManifest.xml`:

   ```xml
   <uses-permission android:name="android.permission.INTERNET" />
   ```

3. **Cleartext Traffic**: للسماح بـ HTTP (localhost):

   ```xml
   <application
       android:usesCleartextTraffic="true">
   ```

4. **معالجة الأخطاء**: تأكد من معالجة جميع الأخطاء المحتملة
5. **التخزين المؤقت**: استخدم Room أو DataStore لتخزين البيانات محلياً

---

## 🚀 الخطوة التالية

بعد الدمج، يمكنك:

1. ✅ عرض قائمة المانجا من المصادر المدعومة
2. ✅ عرض تفاصيل المانجا والفصول
3. ✅ قراءة الفصول مع جميع الصفحات
4. ✅ حفظ المانجا المفضلة محلياً
5. ✅ تتبع تقدم القراءة
