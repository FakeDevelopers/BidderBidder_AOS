package com.fakedevelopers.bidderbidder.ui.register

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fakedevelopers.bidderbidder.ui.register.RegistrationProgressState.ACCEPT_TERMS
import com.fakedevelopers.bidderbidder.ui.register.RegistrationProgressState.EMPTY_STATE
import com.fakedevelopers.bidderbidder.ui.register.RegistrationProgressState.INPUT_ID
import com.fakedevelopers.bidderbidder.ui.register.RegistrationProgressState.INPUT_PASSWORD
import com.fakedevelopers.bidderbidder.ui.register.RegistrationProgressState.PHONE_AUTH_BEFORE_SENDING
import com.fakedevelopers.bidderbidder.ui.register.RegistrationProgressState.PHONE_AUTH_CHECK_AUTH_CODE
import com.fakedevelopers.bidderbidder.ui.util.MutableEventFlow
import com.fakedevelopers.bidderbidder.ui.util.asEventFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class UserRegistrationViewModel : ViewModel() {

    /* AcceptTermsFragment */
    private val _acceptAllState = MutableEventFlow<Boolean>()
    private val essentialTerms = Array(NUMBER_OF_ESSENTIAL_TERM) { false }
    private val optionalTerms = Array(NUMBER_OF_OPTIONAL_TERM) { false }
    val acceptAllState = _acceptAllState.asEventFlow()

    /* PhoneAuthFragment */
    private var phoneAuthToken = ""
    private val _checkAuthCode = MutableEventFlow<Boolean>()
    val checkAuthCode = _checkAuthCode.asEventFlow()
    private var _authCode = MutableStateFlow("")
    val authCode: StateFlow<String> get() = _authCode

    /* UserRegistrationIdFragment */
    private var userId = ""
    var lastDuplicationState = true
        private set
    private val _userIdDuplicationState = MutableEventFlow<Boolean>()
    val userIdDuplicationState = _userIdDuplicationState.asEventFlow()

    private val _userIdValidationState = MutableEventFlow<Boolean>()
    val userIdValidationState = _userIdValidationState.asEventFlow()
    val inputUserId = MutableStateFlow("")

    /* UserRegistrationPasswordFragment */
    private var userPassword = ""
    private val _userPasswordConditionLengthState = MutableStateFlow(false)
    private val _userPasswordConditionCharacterAlphabetState = MutableStateFlow(false)
    private val _userPasswordConditionCharacterNumberState = MutableStateFlow(false)
    private val _userPasswordConfirmState = MutableStateFlow(false)
    private val _userPasswordConfirmVisible = MutableEventFlow<Boolean>()
    val userPasswordConditionLengthState: StateFlow<Boolean> get() = _userPasswordConditionLengthState
    val userPasswordConditionAlphabetState: StateFlow<Boolean> get() = _userPasswordConditionCharacterAlphabetState
    val userPasswordConditionNumberState: StateFlow<Boolean> get() = _userPasswordConditionCharacterNumberState
    val userPasswordConfirmState: StateFlow<Boolean> get() = _userPasswordConfirmState
    val userPasswordConfirmVisible = _userPasswordConfirmVisible.asEventFlow()
    val inputUserPassword = MutableStateFlow("")
    val inputConfirmUserPassword = MutableStateFlow("")

    // 현재 진행 상황
    var currentStep = ACCEPT_TERMS
        private set
    private val _changeRegistrationStep = MutableEventFlow<RegistrationProgressState>()
    private val _nextStepEnabled = MutableStateFlow(false)
    val changeRegistrationStep = _changeRegistrationStep.asEventFlow()
    val nextStepEnabled: StateFlow<Boolean> get() = _nextStepEnabled

    // 실패 토스트 메세지
    private val _failureMessage = MutableEventFlow<String>()
    val failureMessage = _failureMessage.asEventFlow()

    /* AcceptTermsFragment */
    // 모든 약관 동의
    fun setAcceptAllState(isChecked: Boolean) {
        viewModelScope.launch {
            _acceptAllState.emit(isChecked)
        }
    }

    // 약관 선택 상태 변경
    fun setTermState(type: Int, idx: Int, isChecked: Boolean) {
        when (type) {
            TYPE_ESSENTIAL -> essentialTerms[idx] = isChecked
            TYPE_OPTIONAL -> optionalTerms[idx] = isChecked
        }
        if (essentialTerms.all { !it } && optionalTerms.all { !it }) {
            setAcceptAllState(false)
        }
        setNextStepEnabled(checkAcceptTerms())
    }

    // 필수 약관에 모두 동의 했다면 폰 인증 화면으로 넘어갑니다.
    private fun checkAcceptTerms(): Boolean {
        return essentialTerms.all { it }
    }

    // 필수 약관에 모두 동의 했다면 폰 인증 화면으로 넘어갑니다.
    private fun getNextStepOfAccetTerms(): RegistrationProgressState {
        if (essentialTerms.all { it }) {
            return INPUT_ID
        } else {
            sendFailureMessage(NOT_AGREE_TO_ESSENTIAL_TERMS)
            return EMPTY_STATE
        }
    }

    // 코드 조건 확인
    private fun checkNextAuthCode(): Boolean {
        return authCode.value.length == 6
    }

    // 문자 코드 인증 후에 조건 검사 후 다음 단계 반환
    private fun getNextStepOfAuthCheck(): RegistrationProgressState {
        if (checkNextAuthCode()) {
            viewModelScope.launch { _checkAuthCode.emit(true) }
        }
        return EMPTY_STATE
    }

    fun setAuthCode(authCode: String) {
        viewModelScope.launch {
            _authCode.emit(authCode)
            setNextStepEnabled(checkNextAuthCode())
        }
    }

    /* PhoneAuthFragment */
    // 휴대폰 인증 토큰 설정
    fun setPhoneAuthToken(token: String) {
        phoneAuthToken = token
    }

    /* UserRegistrationIdFragment */
    // 아이디 중복 검사
    fun isUserIdDuplicated() {
        // 비어 있다면 검사는 필요 없어
        val regex = "^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}\$".toRegex()
        if (!regex.matches(inputUserId.value)) {
            setUserIdValidationState(true)
            return
        }
        // 여기서는 api를 호출해서 inputId에 중복이 있는지 확인 합니다.
        // 물론 아직 그런건 없으므로 테스트용 EXIST_ID와 같은지만 비교해봅니다.
        lastDuplicationState = inputUserId.value == EXIST_ID
        if (!lastDuplicationState) {
            // 중복이 아니라면 입력칸의 id를 userId로 설정합니다.
            userId = inputUserId.value
        }
        setUserIdDuplicationState(lastDuplicationState)
    }

    // 이미 중복 체크된 아이디거나 중복 체크합니다.
    private fun checkUserId(): Boolean {
        if (!lastDuplicationState && userId == inputUserId.value) {
            return true
        }
        return false
    }

    // 이미 중복 체크된 아이디거나 중복 체크를 통과한 아이디면 다음 단계 반환
    private fun getNextStepOfInputId(): RegistrationProgressState {
        if (checkUserId()) {
            return INPUT_PASSWORD
        } else {
            sendFailureMessage(NOT_ID_DUPLICATION_CHECK)
            return EMPTY_STATE
        }
    }

    // 유효한 아이디 여부 표시
    private fun setUserIdValidationState(state: Boolean) {
        viewModelScope.launch {
            _userIdValidationState.emit(state)
        }
    }

    // 아이디 중복 여부 표시
    private fun setUserIdDuplicationState(state: Boolean) {
        viewModelScope.launch {
            _userIdDuplicationState.emit(state)
        }
        setNextStepEnabled(checkUserId())
    }

    /* UserRegistrationIdFragment */
    // 비밀번호 조건 검사
    fun checkPasswordCondition() {
        val lengthCondition = inputUserPassword.value.length in PASSWORD_LENGTH_MINIMUM..PASSWORD_LENGTH_MAXIMUM
        val alphabetCondition = PASSWORD_ALPHABET_CONDITION.matches(inputUserPassword.value)
        val numberCondition = PASSWORD_NUMBER_CONDITION.matches(inputUserPassword.value)
        viewModelScope.launch {
            _userPasswordConditionLengthState.emit(lengthCondition)
            _userPasswordConditionCharacterAlphabetState.emit(alphabetCondition)
            _userPasswordConditionCharacterNumberState.emit(numberCondition)
            _userPasswordConfirmVisible.emit(lengthCondition && alphabetCondition && numberCondition)
        }
        if (lengthCondition && alphabetCondition && numberCondition) {
            checkPasswordConfirm()
        }
    }

    // 비밀번호 확인
    fun checkPasswordConfirm() {
        viewModelScope.launch {
            _userPasswordConfirmState.emit(inputConfirmUserPassword.value == inputUserPassword.value)
        }
        setNextStepEnabled(checkUserPassword())
    }

    // 비밀번호 검증 (마지막 단계)
    private fun checkUserPassword(): Boolean {
        if (
            userPasswordConditionAlphabetState.value &&
            userPasswordConditionNumberState.value &&
            userPasswordConditionLengthState.value &&
            userPasswordConfirmState.value
        ) {
            userPassword = inputUserPassword.value
            // 가입 완료 판정을 받기 전에 지금까지 모은 정보를 서버에 보내는 작업이 필요합니다.
            // 서버가 ok와 함께 토큰을 던져주고 그걸 저장까지 했을 때 다음 화면으로 넘어갑니다.
            return true
        }
        return false
    }

    //  비밀번호 조건 확인 후 다음 단계 반환
    private fun getNextStepOfInputPassword(): RegistrationProgressState {
        if (checkUserPassword()) {
            return PHONE_AUTH_BEFORE_SENDING
        } else {
            sendFailureMessage(INVALID_REGEX)
            return EMPTY_STATE
        }
    }

    // 다음 단계 버튼 활성화
    fun setNextStepEnabled(state: Boolean) {
        viewModelScope.launch {
            _nextStepEnabled.emit(state)
        }
    }

    // 현재 단계 조건 충족시 다음 단계로 이동
    fun moveNextStep() {
        when (currentStep) {
            ACCEPT_TERMS -> getNextStepOfAccetTerms()
            INPUT_ID -> getNextStepOfInputId()
            INPUT_PASSWORD -> getNextStepOfInputPassword()
            PHONE_AUTH_BEFORE_SENDING -> {
                sendFailureMessage(NOT_RECEIVED_AUTH_CODE)
                EMPTY_STATE
            }
            PHONE_AUTH_CHECK_AUTH_CODE -> getNextStepOfAuthCheck()
            else -> {
                // 여긴 갈 일 없어!
                EMPTY_STATE
            }
        }.let {
            setCurrentStep(it)
        }
    }

    // 단계 설정
    fun setCurrentStep(step: RegistrationProgressState) {
        if (step != EMPTY_STATE) {
            currentStep = step
            viewModelScope.launch {
                _changeRegistrationStep.emit(step)
            }
        }
    }

    // 이전 단계로 돌아가자
    fun toPreviousStep() {
        when (currentStep) {
            INPUT_PASSWORD -> setCurrentStep(INPUT_ID)
            PHONE_AUTH_BEFORE_SENDING -> setCurrentStep(INPUT_PASSWORD)
            PHONE_AUTH_CHECK_AUTH_CODE -> setCurrentStep(INPUT_PASSWORD)
            else -> sendFailureMessage(NOT_GO_BACKWARDS)
        }
    }

    // 실패 토스트 메세지
    private fun sendFailureMessage(msg: String) {
        viewModelScope.launch {
            _failureMessage.emit(msg)
        }
    }

    companion object {
        const val NUMBER_OF_ESSENTIAL_TERM = 4
        const val NUMBER_OF_OPTIONAL_TERM = 1

        // 약관 타입
        const val TYPE_ESSENTIAL = 0
        const val TYPE_OPTIONAL = 1

        // 비밀번호 조건
        const val PASSWORD_LENGTH_MINIMUM = 12
        const val PASSWORD_LENGTH_MAXIMUM = 24

        // 허용되는 특수문자 : !@#$%^+-=
        // 나중에 명확히 정해지면 그걸루 가도 되겠죠
        val PASSWORD_ALPHABET_CONDITION = Regex("^(?=.*[A-Za-z])[a-zA-Z0-9!@#\$%^+\\-=]*$")
        val PASSWORD_NUMBER_CONDITION = Regex("^(?=.*[0-9])[a-zA-Z0-9!@#\$%^+\\-=]*$")

        // 실패 메세지
        const val NOT_GO_BACKWARDS = "가지마!!"
        const val NOT_AGREE_TO_ESSENTIAL_TERMS = "필수 약관에 모두 동의 해!!"
        const val NOT_RECEIVED_AUTH_CODE = "인증번호를 입력해!!"
        const val NOT_ID_DUPLICATION_CHECK = "이게 아이디야?!"

        // 소나가 뭐라캐서 이름을 고칩니다.
        const val INVALID_REGEX = "이게 비밀번호야?!"

        // 테스트용 중복 ID
        const val EXIST_ID = "bidder123@gmail.com"
    }
}
