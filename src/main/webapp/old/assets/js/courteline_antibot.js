/**
 * Courteline Anti-Bot Protection System
 * 
 * Multi-layered client-side bot protection for the booking form
 * Prevents bots from submitting gibberish data to the server
 * 
 * Features:
 * - Honeypot fields
 * - Time-based detection
 * - JavaScript challenge tokens
 * - Form interaction tracking
 * - Input validation (French format)
 * - Gibberish detection
 */

(function() {
  'use strict';

  // Configuration
  const CONFIG = {
    MIN_FORM_TIME: 3,        // Minimum seconds before submission
    MIN_INTERACTIONS: 3,     // Minimum field interactions required
    MAX_NAME_LENGTH: 50,     // Maximum characters in name fields
    MIN_NAME_LENGTH: 2,      // Minimum characters in name fields
    PHONE_PATTERN: /^(\+33|0)[1-9](\d{8})$/,  // French phone format
  };

  // State tracking
  const state = {
    formLoadTime: Date.now(),
    interactions: 0,
    fieldsFocused: new Set(),
    token: null
  };

  // Initialize when DOM is ready
  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', init);
  } else {
    init();
  }

  function init() {
    const form = document.querySelector('form');
    if (!form) {
      console.warn('Courteline: No form found on page');
      return;
    }

    console.log('🛡️ Courteline Anti-Bot Protection activated');
    
    setupHoneypot(form);
    setupJavaScriptChallenge(form);
    setupInteractionTracking(form);
    setupFormValidation(form);
    disableSubmitButton(form);
  }

  /**
   * Add hidden honeypot field that bots will fill but humans won't see
   */
  function setupHoneypot(form) {
    const honeypot = document.createElement('input');
    honeypot.type = 'text';
    honeypot.name = 'website';  // Common field name that bots target
    honeypot.id = 'website';
    honeypot.setAttribute('tabindex', '-1');
    honeypot.setAttribute('autocomplete', 'off');
    honeypot.setAttribute('aria-hidden', 'true');
    honeypot.style.cssText = 'position:absolute;left:-9999px;width:1px;height:1px;opacity:0;';
    
    // Insert at beginning of form
    form.insertBefore(honeypot, form.firstChild);
  }

  /**
   * Add hidden fields for JavaScript challenge
   */
  function setupJavaScriptChallenge(form) {
    // Generate unique token
    state.token = btoa(String(state.formLoadTime) + Math.random());
    
    // Add hidden token field
    const tokenField = document.createElement('input');
    tokenField.type = 'hidden';
    tokenField.name = 'js_token';
    tokenField.id = 'js_token';
    tokenField.value = state.token;
    form.appendChild(tokenField);
    
    // Add hidden timestamp field
    const timeField = document.createElement('input');
    timeField.type = 'hidden';
    timeField.name = 'form_load_time';
    timeField.id = 'form_load_time';
    timeField.value = String(state.formLoadTime);
    form.appendChild(timeField);
  }

  /**
   * Track user interactions to ensure human behavior
   */
  function setupInteractionTracking(form) {
    const fields = form.querySelectorAll('input:not([type="hidden"]), textarea, select');
    const submitBtn = form.querySelector('button[type="submit"], input[type="submit"]');
    
    fields.forEach(field => {
      // Track focus events
      field.addEventListener('focus', function() {
        state.fieldsFocused.add(this.name || this.id);
        state.interactions++;
        
        // Enable submit button after minimum interactions
        if (state.interactions >= CONFIG.MIN_INTERACTIONS && submitBtn) {
          submitBtn.disabled = false;
          submitBtn.style.opacity = '1';
          submitBtn.style.cursor = 'pointer';
        }
      }, { once: true });  // Count each field only once
      
      // Track input events
      field.addEventListener('input', function() {
        state.interactions++;
      });
    });
  }

  /**
   * Disable submit button initially
   */
  function disableSubmitButton(form) {
    const submitBtn = form.querySelector('button[type="submit"], input[type="submit"]');
    if (submitBtn) {
      submitBtn.disabled = true;
      submitBtn.style.opacity = '0.5';
      submitBtn.style.cursor = 'not-allowed';
      submitBtn.setAttribute('data-original-text', submitBtn.textContent);
    }
  }

  /**
   * Main form validation and bot detection
   */
  function setupFormValidation(form) {
    form.addEventListener('submit', function(e) {
      e.preventDefault();
      
      // Run all bot detection checks
      const botChecks = [
        checkHoneypot,
        checkTiming,
        checkJavaScriptToken,
        checkInteractions,
        validateFormData
      ];
      
      for (const check of botChecks) {
        const result = check(form);
        if (!result.passed) {
          console.warn('🤖 Bot detected:', result.reason);
          if (result.showAlert) {
            alert(result.message || 'Veuillez remplir le formulaire correctement.');
          }
          return false;
        }
      }
      
      // All checks passed - allow submission
      console.log('✅ Human verified, submitting form');
      form.submit();
    });
  }

  /**
   * Check if honeypot field was filled (bot behavior)
   */
  function checkHoneypot(form) {
    const honeypot = form.querySelector('#website');
    if (honeypot && honeypot.value !== '') {
      return {
        passed: false,
        reason: 'Honeypot field filled',
        showAlert: false
      };
    }
    return { passed: true };
  }

  /**
   * Check if form was submitted too quickly
   */
  function checkTiming(form) {
    const timeSpent = (Date.now() - state.formLoadTime) / 1000;
    if (timeSpent < CONFIG.MIN_FORM_TIME) {
      return {
        passed: false,
        reason: `Form submitted too quickly (${timeSpent.toFixed(1)}s)`,
        showAlert: false
      };
    }
    return { passed: true };
  }

  /**
   * Verify JavaScript token is present and valid
   */
  function checkJavaScriptToken(form) {
    const tokenField = form.querySelector('#js_token');
    if (!tokenField || tokenField.value !== state.token) {
      return {
        passed: false,
        reason: 'Invalid or missing JavaScript token',
        showAlert: false
      };
    }
    return { passed: true };
  }

  /**
   * Check minimum user interactions occurred
   */
  function checkInteractions(form) {
    if (state.interactions < CONFIG.MIN_INTERACTIONS) {
      return {
        passed: false,
        reason: `Insufficient interactions (${state.interactions}/${CONFIG.MIN_INTERACTIONS})`,
        showAlert: false
      };
    }
    return { passed: true };
  }

  /**
   * Validate actual form data for Courteline booking form
   */
  function validateFormData(form) {
    const validations = [
      validateNom,
      validatePrenom,
      validateEmail,
      validateTelephone,
      validateDate,
      validateNombreNuits,
      validateNombrePersonnes,
      validateChambre
    ];
    
    for (const validate of validations) {
      const result = validate(form);
      if (!result.passed) {
        return result;
      }
    }
    
    return { passed: true };
  }

  /**
   * Validate nom (last name)
   */
  function validateNom(form) {
    const field = form.querySelector('[name="nom"]');
    if (!field) return { passed: true };
    
    const value = field.value.trim();
    
    if (value.length < CONFIG.MIN_NAME_LENGTH || value.length > CONFIG.MAX_NAME_LENGTH) {
      return {
        passed: false,
        reason: 'Invalid nom length',
        message: 'Le nom doit contenir entre 2 et 50 caractères.',
        showAlert: true
      };
    }
    
    if (isGibberish(value)) {
      return {
        passed: false,
        reason: 'Nom appears to be gibberish',
        message: 'Veuillez entrer un nom valide.',
        showAlert: true
      };
    }
    
    return { passed: true };
  }

  /**
   * Validate prenom (first name)
   */
  function validatePrenom(form) {
    const field = form.querySelector('[name="prenom"]');
    if (!field) return { passed: true };
    
    const value = field.value.trim();
    
    if (value.length < CONFIG.MIN_NAME_LENGTH || value.length > CONFIG.MAX_NAME_LENGTH) {
      return {
        passed: false,
        reason: 'Invalid prenom length',
        message: 'Le prénom doit contenir entre 2 et 50 caractères.',
        showAlert: true
      };
    }
    
    if (isGibberish(value)) {
      return {
        passed: false,
        reason: 'Prenom appears to be gibberish',
        message: 'Veuillez entrer un prénom valide.',
        showAlert: true
      };
    }
    
    return { passed: true };
  }

  /**
   * Validate email address
   */
  function validateEmail(form) {
    const field = form.querySelector('[name="email"]');
    if (!field) return { passed: true };
    
    const value = field.value.trim();
    const emailPattern = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    
    if (!emailPattern.test(value)) {
      return {
        passed: false,
        reason: 'Invalid email format',
        message: 'Veuillez entrer une adresse email valide.',
        showAlert: true
      };
    }
    
    // Check for disposable/suspicious email patterns
    if (isSuspiciousEmail(value)) {
      return {
        passed: false,
        reason: 'Suspicious email detected',
        message: 'Veuillez utiliser une adresse email valide.',
        showAlert: true
      };
    }
    
    return { passed: true };
  }

  /**
   * Validate telephone (French format)
   */
  function validateTelephone(form) {
    const field = form.querySelector('[name="telephone"]');
    if (!field) return { passed: true };
    
    const value = field.value.replace(/\s/g, '');  // Remove spaces
    
    if (!CONFIG.PHONE_PATTERN.test(value)) {
      return {
        passed: false,
        reason: 'Invalid phone format',
        message: 'Veuillez entrer un numéro de téléphone français valide (ex: 06 12 34 56 78).',
        showAlert: true
      };
    }
    
    return { passed: true };
  }

  /**
   * Validate date (must be valid and in future)
   */
  function validateDate(form) {
    const field = form.querySelector('[name="date"]');
    if (!field) return { passed: true };
    
    const dateValue = new Date(field.value);
    const today = new Date();
    today.setHours(0, 0, 0, 0);
    
    if (isNaN(dateValue.getTime())) {
      return {
        passed: false,
        reason: 'Invalid date',
        message: 'Veuillez entrer une date valide.',
        showAlert: true
      };
    }
    
    if (dateValue < today) {
      return {
        passed: false,
        reason: 'Date in past',
        message: 'La date de réservation doit être dans le futur.',
        showAlert: true
      };
    }
    
    return { passed: true };
  }

  /**
   * Validate nombre_de_nuits (number of nights)
   */
  function validateNombreNuits(form) {
    const field = form.querySelector('[name="nombre_de_nuits"]');
    if (!field) return { passed: true };
    
    const value = field.value.trim();
    
    if (!/^\d+$/.test(value)) {
      return {
        passed: false,
        reason: 'Invalid nombre_de_nuits format',
        message: 'Le nombre de nuits doit être un nombre.',
        showAlert: true
      };
    }
    
    const num = parseInt(value, 10);
    if (num < 1 || num > 365) {
      return {
        passed: false,
        reason: 'Invalid nombre_de_nuits range',
        message: 'Le nombre de nuits doit être entre 1 et 365.',
        showAlert: true
      };
    }
    
    return { passed: true };
  }

  /**
   * Validate nombre_de_personnes (number of persons)
   */
  function validateNombrePersonnes(form) {
    const field = form.querySelector('[name="nombre_de_personnes"]');
    if (!field) return { passed: true };
    
    const value = field.value.trim();
    
    if (!/^\d+$/.test(value)) {
      return {
        passed: false,
        reason: 'Invalid nombre_de_personnes format',
        message: 'Le nombre de personnes doit être un nombre.',
        showAlert: true
      };
    }
    
    const num = parseInt(value, 10);
    if (num < 1 || num > 20) {
      return {
        passed: false,
        reason: 'Invalid nombre_de_personnes range',
        message: 'Le nombre de personnes doit être entre 1 et 20.',
        showAlert: true
      };
    }
    
    return { passed: true };
  }

  /**
   * Validate chambre_souhaitee (must not be null/empty)
   */
  function validateChambre(form) {
    const field = form.querySelector('[name="chambre_souhaitee"]');
    if (!field) return { passed: true };
    
    const value = field.value.trim();
    
    if (!value || value === 'null' || value === '') {
      return {
        passed: false,
        reason: 'Chambre not selected',
        message: 'Veuillez sélectionner un type de chambre.',
        showAlert: true
      };
    }
    
    return { passed: true };
  }

  /**
   * Detect gibberish text (common bot pattern)
   */
  function isGibberish(text) {
    if (!text || text.length === 0) return true;
    
    // Too many consonants in a row
    if (/[bcdfghjklmnpqrstvwxyz]{6,}/i.test(text)) {
      return true;
    }
    
    // Mixed case pattern like "CXdHWfawnfzYZWsb"
    if (/([A-Z][a-z]{0,2}){4,}/.test(text)) {
      return true;
    }
    
    // Almost no vowels (French names need vowels)
    const vowelCount = (text.match(/[aeiouyàéèêëïôûü]/gi) || []).length;
    const vowelRatio = vowelCount / text.length;
    if (vowelRatio < 0.2) {
      return true;
    }
    
    // All uppercase or all lowercase long strings
    if (text.length > 10 && (text === text.toUpperCase() || text === text.toLowerCase())) {
      const hasSpace = /\s/.test(text);
      if (!hasSpace) return true;
    }
    
    return false;
  }

  /**
   * Detect suspicious email patterns
   */
  function isSuspiciousEmail(email) {
    // Random character patterns
    if (/[a-z]{15,}/i.test(email.split('@')[0])) {
      return true;
    }
    
    // Mixed case in username (unusual)
    const username = email.split('@')[0];
    if (/([a-z][A-Z]|[A-Z][a-z]){3,}/.test(username)) {
      return true;
    }
    
    return false;
  }

})();
