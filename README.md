# Costmatic

기능 구현
 1) 앱실행시 첫 POST 요청
      - BODY 설정값 (start-0 / count-10 / keyword-"" / sub_category_ids-0) 
      - REST API. OkHttp Library 사용
      - 이미지 다운로드 AsyncTask 사용

 2) 동일조건 추가검색 버튼
      - RecyclerView 스크롤 중 마지막 Item일 경우 추가검색 버튼 Visible
      - 동일조건(count / keyword / sub_category_ids) 유지
      - start 조건 변경, start += count
      - 추가검색 받아오면 버튼 다시 Gone (숨기기)
      
 3) 새 조건 검색
      - 사용자 설정 조건 반영 ( Count / Sub_Category_ids / Keyword )
      - 검색 결과 없을 경우, 이전 검색 리스트/조건(BODY) 유지 
      - 검색 결과 있을 경우, 새 결과로 리스트 교체
      
 4) ProgressDialog 구현
      - 검색 중 ProgressDialog
      - 터치 시 Cancel 막음
      
 4) Item 클릭시, 네이버 쇼핑 아이템 검색
