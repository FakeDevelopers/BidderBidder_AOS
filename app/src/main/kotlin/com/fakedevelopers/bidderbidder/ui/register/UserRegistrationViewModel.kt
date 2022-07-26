package com.fakedevelopers.bidderbidder.ui.register

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fakedevelopers.bidderbidder.ui.register.RegistrationProgressState.ACCEPT_TERMS
import com.fakedevelopers.bidderbidder.ui.register.RegistrationProgressState.CONGRATULATIONS
import com.fakedevelopers.bidderbidder.ui.register.RegistrationProgressState.INPUT_BIRTH
import com.fakedevelopers.bidderbidder.ui.register.RegistrationProgressState.INPUT_ID
import com.fakedevelopers.bidderbidder.ui.register.RegistrationProgressState.INPUT_PASSWORD
import com.fakedevelopers.bidderbidder.ui.register.RegistrationProgressState.PHONE_AUTH_BEFORE_SENDING
import com.fakedevelopers.bidderbidder.ui.register.RegistrationProgressState.PHONE_AUTH_CHECK_AUTH_CODE
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class UserRegistrationViewModel : ViewModel() {

    /* AcceptTermsFragment */
    private val _acceptAllState = MutableSharedFlow<Boolean>()
    private val essentialTerms = Array(NUMBER_OF_ESSENTIAL_TERM) { false }
    private val optionalTerms = Array(NUMBER_OF_OPTIONAL_TERM) { false }
    val acceptAllState: SharedFlow<Boolean> get() = _acceptAllState

    /* PhoneAuthFragment */
    private var phoneAuthToken = ""
    private val _checkAuthCode = MutableSharedFlow<Boolean>()
    val checkAuthCode: SharedFlow<Boolean> get() = _checkAuthCode

    /* UserRegistrationBirthFragment */
    private var _birth = MutableStateFlow("")
    val birth: StateFlow<String> get() = _birth

    /* UserRegistrationIdFragment */
    private var userId = ""
    private var lastDuplicationState = true
    private val _userIdDuplicationState = MutableSharedFlow<Boolean>()
    val userIdDuplicationState: SharedFlow<Boolean> get() = _userIdDuplicationState
    val inputUserId = MutableStateFlow("")

    /* UserRegistrationPasswordFragment */
    private var userPassword = ""
    private val _userPasswordConditionLengthState = MutableStateFlow(false)
    private val _userPasswordConditionCharacterState = MutableStateFlow(false)
    private val _userPasswordConfirmState = MutableStateFlow(false)
    private val _userPasswordConfirmVisible = MutableSharedFlow<Boolean>()
    val userPasswordConditionLengthState: StateFlow<Boolean> get() = _userPasswordConditionLengthState
    val userPasswordConditionCharacterState: StateFlow<Boolean> get() = _userPasswordConditionCharacterState
    val userPasswordConfirmState: StateFlow<Boolean> get() = _userPasswordConfirmState
    val userPasswordConfirmVisible: SharedFlow<Boolean> get() = _userPasswordConfirmVisible
    val inputUserPassword = MutableStateFlow("")
    val inputConfirmUserPassword = MutableStateFlow("")

    // 현재 진행 상황
    private var currentStep = ACCEPT_TERMS
    private val _changeRegistrationStep = MutableSharedFlow<RegistrationProgressState>()
    private val _nextStepEnabled = MutableStateFlow(true)
    val changeRegistrationStep: SharedFlow<RegistrationProgressState> get() = _changeRegistrationStep
    val nextStepEnabled: StateFlow<Boolean> get() = _nextStepEnabled

    // 실패 토스트 메세지
    private val _failureMessage = MutableSharedFlow<String>()
    val failureMessage: SharedFlow<String> get() = _failureMessage

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
    }

    // 필수 약관에 모두 동의 했다면 폰 인증 화면으로 넘어갑니다.
    private fun checkAcceptTerms() {
        if (essentialTerms.all { it }) {
            setCurrentStep(PHONE_AUTH_BEFORE_SENDING)
        } else {
            sendFailureMessage(NOT_AGREE_TO_ESSENTIAL_TERMS)
        }
    }

    /* PhoneAuthFragment */
    // 휴대폰 인증 토큰 설정
    fun setPhoneAuthToken(token: String) {
        phoneAuthToken = token
    }

    /* UserRegistrationBirthFragment */
    // 생년월일 설정
    fun setBirth(birth: String) {
        viewModelScope.launch {
            _birth.emit(birth)
        }
    }

    // 지금은 단순히 생년월일로 뭔갈 넣었다면
    private fun checkBirth() {
        if (birth.value.isNotEmpty()) {
            setCurrentStep(INPUT_ID)
        } else {
            sendFailureMessage(EMPTY_BIRTH)
        }
    }

    /* UserRegistrationIdFragment */
    // 아이디 중복 검사
    fun isUserIdDuplicated(): Boolean {
        // 비어 있다면 검사는 필요 없어
        if (inputUserId.value.isEmpty()) {
            return true
        }
        // 여기서는 api를 호출해서 inputId에 중복이 있는지 확인 합니다.
        // 물론 아직 그런건 없으므로 테스트용 EXIST_ID와 같은지만 비교해봅니다.
        lastDuplicationState = inputUserId.value == EXIST_ID
        if (lastDuplicationState) {
            // 중복이 아니라면 입력칸의 id를 userId로 설정합니다.
            userId = inputUserId.value
        }
        setUserIdDuplicationState(lastDuplicationState)
        return lastDuplicationState
    }

    // 이미 중복 체크된 아이디거나 중복 체크를 통과한 아이디면 다음 단계로 갑니다.
    private fun checkUserId() {
        if (!lastDuplicationState && (userId == inputUserId.value || !isUserIdDuplicated())) {
            setCurrentStep(INPUT_PASSWORD)
        } else {
            sendFailureMessage(NOT_ID_DUPLICATION_CHECK)
        }
    }

    // 아이디 중복 여부 표시
    private fun setUserIdDuplicationState(state: Boolean) {
        viewModelScope.launch {
            _userIdDuplicationState.emit(state)
        }
    }

    /* UserRegistrationIdFragment */
    // 비밀번호 조건 검사
    fun checkPasswordCondition() {
        val lengthCondition = inputUserPassword.value.length in PASSWORD_LENGTH_MINIMUM..PASSWORD_LENGTH_MAXIMUM
        val characterCondition = PASSWORD_CHARACTER_CONDITION.matchEntire(inputUserPassword.value) != null
        viewModelScope.launch {
            _userPasswordConditionLengthState.emit(lengthCondition)
            _userPasswordConditionCharacterState.emit(characterCondition)
            _userPasswordConfirmVisible.emit(lengthCondition && characterCondition)
        }
    }

    // 비밀번호 확인
    fun checkPasswordConfirm() {
        viewModelScope.launch {
            _userPasswordConfirmState.emit(inputConfirmUserPassword.value == inputUserPassword.value)
        }
    }

    // 비밀번호 검증 (마지막 단계)
    private fun checkUserPassword() {
        if (
            userPasswordConditionCharacterState.value &&
            userPasswordConditionLengthState.value &&
            userPasswordConfirmState.value
        ) {
            userPassword = inputUserPassword.value
            // 가입 완료 판정을 받기 전에 지금까지 모은 정보를 서버에 보내는 작업이 필요합니다.
            // 서버가 ok와 함께 토큰을 던져주고 그걸 저장까지 했을 때 다음 화면으로 넘어갑니다.
            setCurrentStep(CONGRATULATIONS)
        } else {
            sendFailureMessage(INVALID_PASSWORD)
        }
    }

    // 다음 단계 버튼 활성화
    fun setNextStepEnabled(state: Boolean) {
        viewModelScope.launch {
            _nextStepEnabled.emit(state)
        }
    }

    // 다음 단계를 가기 전 검증
    fun checkNextStep() {
        when (currentStep) {
            ACCEPT_TERMS -> checkAcceptTerms()
            PHONE_AUTH_BEFORE_SENDING -> sendFailureMessage(NOT_RECEIVED_AUTH_CODE)
            PHONE_AUTH_CHECK_AUTH_CODE -> viewModelScope.launch { _checkAuthCode.emit(true) }
            INPUT_BIRTH -> checkBirth()
            INPUT_ID -> checkUserId()
            INPUT_PASSWORD -> checkUserPassword()
            else -> {
                // 여긴 갈 일 없어!
            }
        }
    }

    // 단계 설정
    fun setCurrentStep(step: RegistrationProgressState) {
        currentStep = step
        viewModelScope.launch {
            _changeRegistrationStep.emit(step)
        }
    }

    // 이전 단계로 돌아가자
    fun toPreviousStep() {
        when (currentStep) {
            INPUT_ID -> setCurrentStep(INPUT_BIRTH)
            INPUT_PASSWORD -> setCurrentStep(INPUT_ID)
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
        val PASSWORD_CHARACTER_CONDITION = Regex("^(?=.*[A-Za-z])(?=.*[0-9])[a-zA-Z0-9]*$")

        // 실패 메세지
        const val NOT_GO_BACKWARDS = "가지마!!"
        const val NOT_AGREE_TO_ESSENTIAL_TERMS = "필수 약관에 모두 동의 해!!"
        const val NOT_RECEIVED_AUTH_CODE = "인증번호를 입력해!!"
        const val EMPTY_BIRTH = "생년월일이 없잖아!!"
        const val NOT_ID_DUPLICATION_CHECK = "이게 아이디야?!"
        const val INVALID_PASSWORD = "이게 비밀번호야?!"

        // 테스트용 중복 ID
        const val EXIST_ID = "bidder123"
    }
}
